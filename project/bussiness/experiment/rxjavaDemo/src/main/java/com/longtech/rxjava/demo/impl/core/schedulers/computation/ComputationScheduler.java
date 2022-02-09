package com.longtech.rxjava.demo.impl.core.schedulers.computation;

import androidx.annotation.NonNull;

import com.longtech.rxjava.demo.impl.core.thread.RxThreadFactory;
import com.longtech.rxjava.demo.impl.core.Scheduler;
import com.longtech.rxjava.demo.impl.core.SchedulerMultiWorkerSupport;
import com.longtech.rxjava.demo.impl.core.dispose.CompositeDisposable;
import com.longtech.rxjava.demo.impl.core.dispose.Disposable;
import com.longtech.rxjava.demo.impl.core.dispose.EmptyDisposable;
import com.longtech.rxjava.demo.impl.core.dispose.ListCompositeDisposable;
import com.longtech.rxjava.demo.impl.core.schedulers.newthread.NewThreadWorker;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ComputationScheduler   1:n  EventLoopWorker  1:1   FixedSchedulerPool  1:n  PoolWorker  1:1  NewThreadWorker   1:1   ScheduledExecutorService
 */
public final class ComputationScheduler extends Scheduler implements SchedulerMultiWorkerSupport {
    /** This will indicate no pool is active. */
    static final ComputationScheduler.FixedSchedulerPool NONE;
    /** Manages a fixed number of workers. */
    private static final String THREAD_NAME_PREFIX = "RxComputationThreadPool";
    static final RxThreadFactory THREAD_FACTORY;
    /**
     * Key to setting the maximum number of computation scheduler threads.
     * Zero or less is interpreted as use available. Capped by available.
     */
    static final String KEY_MAX_THREADS = "rx3.computation-threads";
    /** The maximum number of computation scheduler threads. */
    static final int MAX_THREADS;

    static final ComputationScheduler.PoolWorker SHUTDOWN_WORKER;

    final ThreadFactory threadFactory;
    final AtomicReference<ComputationScheduler.FixedSchedulerPool> pool;
    /** The name of the system property for setting the thread priority for this Scheduler. */
    private static final String KEY_COMPUTATION_PRIORITY = "rx3.computation-priority";

    static {
        MAX_THREADS = cap(Runtime.getRuntime().availableProcessors(), Integer.getInteger(KEY_MAX_THREADS, 0));

        SHUTDOWN_WORKER = new ComputationScheduler.PoolWorker(new RxThreadFactory("RxComputationShutdown"));
        SHUTDOWN_WORKER.dispose();

        int priority = Math.max(Thread.MIN_PRIORITY, Math.min(Thread.MAX_PRIORITY,
                Integer.getInteger(KEY_COMPUTATION_PRIORITY, Thread.NORM_PRIORITY)));

        THREAD_FACTORY = new RxThreadFactory(THREAD_NAME_PREFIX, priority, true);

        NONE = new ComputationScheduler.FixedSchedulerPool(0, THREAD_FACTORY);
        NONE.shutdown();
    }

    static int cap(int cpuCount, int paramThreads) {
        return paramThreads <= 0 || paramThreads > cpuCount ? cpuCount : paramThreads;
    }

    static final class FixedSchedulerPool implements SchedulerMultiWorkerSupport {
        final int cores;

        final ComputationScheduler.PoolWorker[] eventLoops;
        long n;

        FixedSchedulerPool(int maxThreads, ThreadFactory threadFactory) {
            // initialize event loops
            this.cores = maxThreads;
            this.eventLoops = new ComputationScheduler.PoolWorker[maxThreads];
            for (int i = 0; i < maxThreads; i++) {
                this.eventLoops[i] = new ComputationScheduler.PoolWorker(threadFactory);
            }
        }

        public ComputationScheduler.PoolWorker getEventLoop() {
            int c = cores;
            if (c == 0) {
                return SHUTDOWN_WORKER;
            }
            // simple round robin, improvements to come
            return eventLoops[(int)(n++ % c)];
        }

        public void shutdown() {
            for (ComputationScheduler.PoolWorker w : eventLoops) {
                w.dispose();
            }
        }

        @Override
        public void createWorkers(int number, WorkerCallback callback) {
            int c = cores;
            if (c == 0) {
                for (int i = 0; i < number; i++) {
                    callback.onWorker(i, SHUTDOWN_WORKER);
                }
            } else {
                int index = (int)n % c;
                for (int i = 0; i < number; i++) {
                    callback.onWorker(i, new ComputationScheduler.EventLoopWorker(eventLoops[index]));
                    if (++index == c) {
                        index = 0;
                    }
                }
                n = index;
            }
        }
    }

    /**
     * Create a scheduler with pool size equal to the available processor
     * count and using least-recent worker selection policy.
     */
    public ComputationScheduler() {
        this(THREAD_FACTORY);
    }

    /**
     * Create a scheduler with pool size equal to the available processor
     * count and using least-recent worker selection policy.
     *
     * @param threadFactory thread factory to use for creating worker threads. Note that this takes precedence over any
     *                      system properties for configuring new thread creation. Cannot be null.
     */
    public ComputationScheduler(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        this.pool = new AtomicReference<>(NONE);
        start();
    }

    /**
     * 新建一个Worker，其中的pool中维护了固定数量的PoolWorker，每个Worker都是一个NewThreadWorker（ScheduledThreadPoolExecutor）
     * EventLoopWorker     ->     FixedSchedulerPool.get()   ->   PoolWorker(NewThreadWorker)    ->    ScheduledThreadPoolExecutor
     *
     * @return
     */
    @Override
    public Worker createWorker() {
        return new ComputationScheduler.EventLoopWorker(pool.get().getEventLoop());
    }

    @Override
    public void createWorkers(int number, WorkerCallback callback) {
        //ObjectHelper.verifyPositive(number, "number > 0 required");
        pool.get().createWorkers(number, callback);
    }

    @NonNull
    @Override
    public Disposable scheduleDirect(@NonNull Runnable run, long delay, TimeUnit unit) {
        ComputationScheduler.PoolWorker w = pool.get().getEventLoop();
        return w.scheduleDirect(run, delay, unit);
    }

    /**
     * 如果是NONE，就更新为update
     */
    @Override
    public void start() {
        ComputationScheduler.FixedSchedulerPool update = new ComputationScheduler.FixedSchedulerPool(MAX_THREADS, threadFactory);
        if (!pool.compareAndSet(NONE, update)) {
            update.shutdown();
        }
    }

    /**
     * 如果当前不是NONE，要先shotdown
     */
    @Override
    public void shutdown() {
        ComputationScheduler.FixedSchedulerPool curr = pool.getAndSet(NONE);
        if (curr != NONE) {
            curr.shutdown();
        }
    }

    static final class EventLoopWorker extends Scheduler.Worker {
        private final ListCompositeDisposable serial;
        private final CompositeDisposable timed;
        private final ListCompositeDisposable both;
        private final ComputationScheduler.PoolWorker poolWorker;

        volatile boolean disposed;

        EventLoopWorker(ComputationScheduler.PoolWorker poolWorker) {
            this.poolWorker = poolWorker;
            this.serial = new ListCompositeDisposable();
            this.timed = new CompositeDisposable();
            this.both = new ListCompositeDisposable();
            this.both.add(serial);
            this.both.add(timed);
        }

        @Override
        public void dispose() {
            if (!disposed) {
                disposed = true;
                both.dispose();
            }
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }

        @NonNull
        @Override
        public Disposable schedule(@NonNull Runnable action) {
            if (disposed) {
                return EmptyDisposable.INSTANCE;
            }

            return poolWorker.scheduleActual(action, 0, TimeUnit.MILLISECONDS, serial);
        }

        @NonNull
        @Override
        public Disposable schedule(@NonNull Runnable action, long delayTime, @NonNull TimeUnit unit) {
            if (disposed) {
                return EmptyDisposable.INSTANCE;
            }

            return poolWorker.scheduleActual(action, delayTime, unit, timed);
        }
    }

    static final class PoolWorker extends NewThreadWorker {
        PoolWorker(ThreadFactory threadFactory) {
            super(threadFactory);
        }
    }
}