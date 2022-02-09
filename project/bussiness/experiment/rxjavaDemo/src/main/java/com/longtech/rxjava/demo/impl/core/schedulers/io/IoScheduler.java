package com.longtech.rxjava.demo.impl.core.schedulers.io;

import androidx.annotation.NonNull;

import com.longtech.rxjava.demo.impl.core.thread.RxThreadFactory;
import com.longtech.rxjava.demo.impl.core.Scheduler;
import com.longtech.rxjava.demo.impl.core.dispose.CompositeDisposable;
import com.longtech.rxjava.demo.impl.core.dispose.Disposable;
import com.longtech.rxjava.demo.impl.core.dispose.EmptyDisposable;
import com.longtech.rxjava.demo.impl.core.schedulers.newthread.NewThreadWorker;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * IoScheduler   1:n  EventLoopWorker  1:1   CachedWorkerPool  1:n  ThreadWorker  1:1  NewThreadWorker   1:1   ScheduledExecutorService
 */
public final class IoScheduler extends Scheduler {
    private static final String WORKER_THREAD_NAME_PREFIX = "RxCachedThreadScheduler";
    static final RxThreadFactory WORKER_THREAD_FACTORY;

    private static final String EVICTOR_THREAD_NAME_PREFIX = "RxCachedWorkerPoolEvictor";
    static final RxThreadFactory EVICTOR_THREAD_FACTORY;

    /** The name of the system property for setting the keep-alive time (in seconds) for this Scheduler workers. */
    private static final String KEY_KEEP_ALIVE_TIME = "rx3.io-keep-alive-time";
    public static final long KEEP_ALIVE_TIME_DEFAULT = 60;

    private static final long KEEP_ALIVE_TIME;
    private static final TimeUnit KEEP_ALIVE_UNIT = TimeUnit.SECONDS;

    static final ThreadWorker SHUTDOWN_THREAD_WORKER;
    final ThreadFactory threadFactory;
    final AtomicReference<CachedWorkerPool> pool;

    /** The name of the system property for setting the thread priority for this Scheduler. */
    private static final String KEY_IO_PRIORITY = "rx3.io-priority";

    /** The name of the system property for setting the release behaviour for this Scheduler. */
    private static final String KEY_SCHEDULED_RELEASE = "rx3.io-scheduled-release";
    static boolean USE_SCHEDULED_RELEASE;

    static final CachedWorkerPool NONE;

    static {
        KEEP_ALIVE_TIME = Long.getLong(KEY_KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_DEFAULT);

        SHUTDOWN_THREAD_WORKER = new ThreadWorker(new RxThreadFactory("RxCachedThreadSchedulerShutdown"));
        SHUTDOWN_THREAD_WORKER.dispose();

        int priority = Math.max(Thread.MIN_PRIORITY, Math.min(Thread.MAX_PRIORITY,
                Integer.getInteger(KEY_IO_PRIORITY, Thread.NORM_PRIORITY)));

        WORKER_THREAD_FACTORY = new RxThreadFactory(WORKER_THREAD_NAME_PREFIX, priority);

        EVICTOR_THREAD_FACTORY = new RxThreadFactory(EVICTOR_THREAD_NAME_PREFIX, priority);

        USE_SCHEDULED_RELEASE = Boolean.getBoolean(KEY_SCHEDULED_RELEASE);

        NONE = new CachedWorkerPool(0, null, WORKER_THREAD_FACTORY);
        NONE.shutdown();
    }

    static final class CachedWorkerPool implements Runnable {
        private final long keepAliveTime;
        private final ConcurrentLinkedQueue<ThreadWorker> expiringWorkerQueue;
        final CompositeDisposable allWorkers;
        private final ScheduledExecutorService evictorService;
        private final Future<?> evictorTask;
        private final ThreadFactory threadFactory;

        CachedWorkerPool(long keepAliveTime, TimeUnit unit, ThreadFactory threadFactory) {
            this.keepAliveTime = unit != null ? unit.toNanos(keepAliveTime) : 0L;
            this.expiringWorkerQueue = new ConcurrentLinkedQueue<>();
            this.allWorkers = new CompositeDisposable();
            this.threadFactory = threadFactory;

            /**
             * 开启了定时器
             */
            ScheduledExecutorService evictor = null;
            Future<?> task = null;
            if (unit != null) {
                evictor = Executors.newScheduledThreadPool(1, EVICTOR_THREAD_FACTORY);
                task = evictor.scheduleWithFixedDelay(this, this.keepAliveTime, this.keepAliveTime, TimeUnit.NANOSECONDS);
            }
            evictorService = evictor;
            evictorTask = task;
        }

        /**
         * [定时清理]：定时器用于定时检查一下，是否Worker释放之后过期了，过期了之后移除expiringWorkerQueue，同时allWorkers
         */
        @Override
        public void run() {

            evictExpiredWorkers(expiringWorkerQueue, allWorkers);
        }

        /**
         * [获取]：可以把expiringWorkerQueue理解成Worker缓存池
         * @return
         */
        ThreadWorker get() {
            // 如果所有的Worker都释放了，那么返回一个Shutdown的ThreadWorker
            if (allWorkers.isDisposed()) {
                return SHUTDOWN_THREAD_WORKER;
            }
            // 从过期的WorkerQueue队列中取出来一个
            while (!expiringWorkerQueue.isEmpty()) {
                ThreadWorker threadWorker = expiringWorkerQueue.poll();
                if (threadWorker != null) {
                    return threadWorker;
                }
            }

            // 若过期的WorkerQueue队列中没有，那么新建一个
            ThreadWorker w = new ThreadWorker(threadFactory);
            allWorkers.add(w);
            return w;
        }

        /**
         * [释放]：释放回缓存池中的时候，设置一下超时时间
         * @param threadWorker
         */
        void release(ThreadWorker threadWorker) {
            // Refresh expire time before putting worker back in pool
            threadWorker.setExpirationTime(now() + keepAliveTime);

            expiringWorkerQueue.offer(threadWorker);
        }

        static void evictExpiredWorkers(ConcurrentLinkedQueue<ThreadWorker> expiringWorkerQueue, CompositeDisposable allWorkers) {
            if (!expiringWorkerQueue.isEmpty()) {
                long currentTimestamp = now();

                for (ThreadWorker threadWorker : expiringWorkerQueue) {
                    if (threadWorker.getExpirationTime() <= currentTimestamp) {
                        if (expiringWorkerQueue.remove(threadWorker)) {
                            allWorkers.remove(threadWorker);
                        }
                    } else {
                        // Queue is ordered with the worker that will expire first in the beginning, so when we
                        // find a non-expired worker we can stop evicting.
                        break;
                    }
                }
            }
        }

        static long now() {
            return System.nanoTime();
        }

        void shutdown() {
            allWorkers.dispose();
            if (evictorTask != null) {
                evictorTask.cancel(true);
            }
            if (evictorService != null) {
                evictorService.shutdownNow();
            }
        }
    }

    public IoScheduler() {
        this(WORKER_THREAD_FACTORY);
    }

    /**
     * Constructs an IoScheduler with the given thread factory and starts the pool of workers.
     * @param threadFactory thread factory to use for creating worker threads. Note that this takes precedence over any
     *                      system properties for configuring new thread creation. Cannot be null.
     */
    public IoScheduler(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        this.pool = new AtomicReference<>(NONE);
        start();
    }

    /**
     * 设置pool为update（如果pool不是NONE，那么是不能设置的，代表上一个线程池还在执行中）
     */
    @Override
    public void start() {
        CachedWorkerPool update = new CachedWorkerPool(KEEP_ALIVE_TIME, KEEP_ALIVE_UNIT, threadFactory);
        if (!pool.compareAndSet(NONE, update)) {
            update.shutdown();
        }
    }

    /**
     * 设置pool为NONE
     */
    @Override
    public void shutdown() {
        CachedWorkerPool curr = pool.getAndSet(NONE);
        if (curr != NONE) {
            curr.shutdown();
        }
    }

    /**
     * EventLoopWorker     ->     CachedWorkerPool.get()   ->     ThreadWorker    ->    NewThreadWorker(ScheduledThreadPoolExecutor)
     * @return
     */
    @Override
    public Worker createWorker() {
        return new EventLoopWorker(pool.get());
    }

    public int size() {
        return pool.get().allWorkers.size();
    }

    /**
     * EventLoopWorker本质上threadWorker的一个代理
     * 1、threadWorker是从CachedWorkerPool中获取的
     * 2、threadWorker调用schedule(runnable)执行任务
     * 3、threadWorker调用release释放自己
     */
    static final class EventLoopWorker extends Scheduler.Worker implements Runnable {
        private final CompositeDisposable tasks;
        private final CachedWorkerPool pool;
        private final ThreadWorker threadWorker;

        final AtomicBoolean once = new AtomicBoolean();

        EventLoopWorker(CachedWorkerPool pool) {
            this.pool = pool;
            this.tasks = new CompositeDisposable();
            this.threadWorker = pool.get();
        }

        @Override
        public void dispose() {
            if (once.compareAndSet(false, true)) {
                tasks.dispose();

                if (USE_SCHEDULED_RELEASE) {
                    threadWorker.scheduleActual(this, 0, TimeUnit.NANOSECONDS, null);
                } else {
                    // releasing the pool should be the last action
                    pool.release(threadWorker);
                }
            }
        }

        @Override
        public void run() {
            pool.release(threadWorker);
        }

        @Override
        public boolean isDisposed() {
            return once.get();
        }

        @NonNull
        @Override
        public Disposable schedule(@NonNull Runnable action, long delayTime, @NonNull TimeUnit unit) {
            if (tasks.isDisposed()) {
                // don't schedule, we are unsubscribed
                return EmptyDisposable.INSTANCE;
            }

            return threadWorker.scheduleActual(action, delayTime, unit, tasks);
        }
    }

    static final class ThreadWorker extends NewThreadWorker {

        long expirationTime;

        ThreadWorker(ThreadFactory threadFactory) {
            super(threadFactory);
            this.expirationTime = 0L;
        }

        public long getExpirationTime() {
            return expirationTime;
        }

        public void setExpirationTime(long expirationTime) {
            this.expirationTime = expirationTime;
        }
    }
}
