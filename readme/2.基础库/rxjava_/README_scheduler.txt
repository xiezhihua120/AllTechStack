----------------------------------------------------------------------------------------------------
Q：线程切换统一抽象到Scheduler中，是如何做的（How）？

拆解一：Scheduler结构

    1.1 ------------------------------------------------
    [Scheduler.java]
    public abstract class Scheduler {

        public abstract Worker createWorker();
    
        public void start() {}
    
        public void shutdown() {}
    
        public Disposable scheduleDirect(Runnable run) {
            return scheduleDirect(run, 0L, TimeUnit.NANOSECONDS);
        }
    
        public Disposable scheduleDirect(Runnable run, long delay, TimeUnit unit) {
            final Worker w = createWorker();
            final Runnable decoratedRun = RxJavaPlugins.onSchedule(run);
            DisposeTask task = new DisposeTask(decoratedRun, w);
            w.schedule(task, delay, unit);
            return task;
        }

    }
    分析：Scheduler调度器，本身管理的对象是一个个Worker；而Worker代表了一个个执行任务的员工
         其中Runnable代表原始任务，而DisposeTask代表可取消的任务，是对任务的一层包装增强
         Worker这里是逻辑上的抽象，到底层细节上肯定是由线程池来执行任务的，因此Worker中要包括一个CachedWorkPool
    结论：1、Scheduler是一个整体调度器，相当于一个领导，手下有很多的Worker员工，任务来了之后他分配给员工干。
         2、在员工干之前，需要把任务整理一下，runnable -> DisposeTask -> ScheduleRunnable
         3、因为Runnable里面本身就包含工作（生产）和报告（消费），所以Scheduler领导分配完成之后就基本不用管了。
         4、Runnable会自觉的工作，并把进度告诉给对应的它的汇报者（消费者）

    1.2 ------------------------------------------------
    [Worker.java]
    public abstract static class Worker implements Disposable {
    
        public Disposable schedule(Runnable run) {
            return schedule(run, 0L, TimeUnit.NANOSECONDS);
        }
    
        public abstract Disposable schedule(Runnable run, long delay, TimeUnit unit);
    
    }
    分析：Worker代表了一个员工，任务是靠员工来完成的，因此最重要的就是Worker.schedule(runnable)接口
         这里比较特殊的，Worker可以实现取消操作，让一个正在执行的任务停止了

    1.3 ------------------------------------------------
    [DisposeTask.java]
    static final class DisposeTask implements Disposable, Runnable, SchedulerRunnableIntrospection {

        final Runnable decoratedRun;

        final Worker w;

        Thread runner;

        DisposeTask(Runnable decoratedRun, Worker w) {
            this.decoratedRun = decoratedRun;
            this.w = w;
        }

        @Override
        public void run() {
            runner = Thread.currentThread();
            try {
                decoratedRun.run();
            } finally {
                dispose();
                runner = null;
            }
        }

        @Override
        public void dispose() {
            if (runner == Thread.currentThread() && w instanceof NewThreadWorker) {
                ((NewThreadWorker)w).shutdown();
            } else {
                w.dispose();
            }
        }

        @Override
        public boolean isDisposed() {
            return w.isDisposed();
        }

        @Override
        public Runnable getWrappedRunnable() {
            return this.decoratedRun;
        }

    }
    分析：原始的任务其实是经过拆解和安排的（执行逻辑、汇报节点都安排好了），这里对任务进行了加强，可以随时执行取消操作
         1、一个DisposeTask包括runnable和worker两部分，runnable是实际干活的，worker是用来取消任务的


拆解二：IOScheduler分析

    2.1 ------------------------------------------------
    [CachedWorkerPool.java]
    static final class CachedWorkerPool implements Runnable {
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

            ScheduledExecutorService evictor = null;
            Future<?> task = null;
            if (unit != null) {
                evictor = Executors.newScheduledThreadPool(1, EVICTOR_THREAD_FACTORY);
                task = evictor.scheduleWithFixedDelay(this, this.keepAliveTime, this.keepAliveTime, TimeUnit.NANOSECONDS);
            }
            evictorService = evictor;
            evictorTask = task;
        }

        @Override
        public void run() {
            evictExpiredWorkers(expiringWorkerQueue, allWorkers);
        }
        分析：定时清理超时的工作队列

        ThreadWorker get() {
            if (allWorkers.isDisposed()) {
                return SHUTDOWN_THREAD_WORKER;
            }
            while (!expiringWorkerQueue.isEmpty()) {
                ThreadWorker threadWorker = expiringWorkerQueue.poll();
                if (threadWorker != null) {
                    return threadWorker;
                }
            }

            // No cached worker found, so create a new one.
            ThreadWorker w = new ThreadWorker(threadFactory);
            allWorkers.add(w);
            return w;
        }
    }

    2.2 ------------------------------------------------
    [ThreadWorker.java]
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
    分析：带有超时时间的NewThreadWorker

    2.3 ------------------------------------------------
    [NewThreadWorker.java]
    public class NewThreadWorker extends Scheduler.Worker implements Disposable {
        private final ScheduledExecutorService executor;

        volatile boolean disposed;

        public NewThreadWorker(ThreadFactory threadFactory) {
            executor = SchedulerPoolFactory.create(threadFactory);
        }

        @NonNull
        @Override
        public Disposable schedule(final Runnable run) {
            return schedule(run, 0, null);
        }

        @NonNull
        @Override
        public Disposable schedule(final Runnable action, long delayTime, TimeUnit unit) {
            return scheduleActual(action, delayTime, unit, null);
        }

        public Disposable scheduleDirect(final Runnable run, long delayTime, TimeUnit unit) {
            ScheduledDirectTask task = new ScheduledDirectTask(RxJavaPlugins.onSchedule(run));
            try {
                Future<?> f;
                if (delayTime <= 0L) {
                    f = executor.submit(task);
                } else {
                    f = executor.schedule(task, delayTime, unit);
                }
                task.setFuture(f);
                return task;
            } catch (RejectedExecutionException ex) {
                RxJavaPlugins.onError(ex);
                return EmptyDisposable.INSTANCE;
            }
        }
        分析：ScheduledDirectTask这里的task仍然是对runnable、future的一个封装，它是可以取消的。还是仔细分析一下吧
             通过以下的分析可知，提交了一个任务，任务支持了FINISHED、DISPOSE、future三种状态
             2.2.1 -----------------------------------------
             public final class ScheduledDirectTask extends AbstractDirectTask implements Callable<Void> {

                 public ScheduledDirectTask(Runnable runnable) {
                     super(runnable);
                 }

                 @Override
                 public Void call() {
                     runner = Thread.currentThread();
                     try {
                         runnable.run();
                     } finally {
                         lazySet(FINISHED);
                         runner = null;
                     }
                     return null;
                 }
             }
             分析：首先它是一个Callable，在ExcutorService中是可以执行的，另外她支持了设置状态FINISHED

             2.2.2 -----------------------------------------
             abstract class AbstractDirectTask extends AtomicReference<Future<?>> implements Disposable, SchedulerRunnableIntrospection {
                 protected final Runnable runnable;

                 protected Thread runner;

                 protected static final FutureTask<Void> FINISHED = new FutureTask<>(Functions.EMPTY_RUNNABLE, null);

                 protected static final FutureTask<Void> DISPOSED = new FutureTask<>(Functions.EMPTY_RUNNABLE, null);

                 AbstractDirectTask(Runnable runnable) {
                     this.runnable = runnable;
                 }

                 @Override
                 public final void dispose() {
                     Future<?> f = get();
                     if (f != FINISHED && f != DISPOSED) {
                         if (compareAndSet(f, DISPOSED)) {
                             if (f != null) {
                                 f.cancel(runner != Thread.currentThread());
                             }
                         }
                     }
                 }

                 @Override
                 public final boolean isDisposed() {
                     Future<?> f = get();
                     return f == FINISHED || f == DISPOSED;
                 }

                 public final void setFuture(Future<?> future) {
                     for (;;) {
                         Future<?> f = get();
                         if (f == FINISHED) {
                             break;
                         }
                         if (f == DISPOSED) {
                             future.cancel(runner != Thread.currentThread());
                             break;
                         }
                         if (compareAndSet(f, future)) {
                             break;
                         }
                     }
                 }

                 @Override
                 public Runnable getWrappedRunnable() {
                     return runnable;
                 }
             }
             分析：这里看不明白，用两个变量不香吗？不知道为啥要用Future
                  大概看明白了，这里的ScheduleDirectTask，既在内部持有了runnable，又持有了ExecutorService.submit产生的future，还支持dispose操作
                  为了统一，它将所有的状态都抽象成了Future
                  1、future   代表了任务执行中
                  2、FINISHED 代表了任务结束
                  3、DISPOSED 代表了任务取消

                  这种抽象方法，其实感受其他还是不错的！

        2.3 ------------------------------------------------
        public ScheduledRunnable scheduleActual(final Runnable run, long delayTime, TimeUnit unit, DisposableContainer parent) {
            Runnable decoratedRun = RxJavaPlugins.onSchedule(run);

            ScheduledRunnable sr = new ScheduledRunnable(decoratedRun, parent);

            if (parent != null) {
                if (!parent.add(sr)) {
                    return sr;
                }
            }

            Future<?> f;
            try {
                if (delayTime <= 0) {
                    f = executor.submit((Callable<Object>)sr);
                } else {
                    f = executor.schedule((Callable<Object>)sr, delayTime, unit);
                }
                sr.setFuture(f);
            } catch (RejectedExecutionException ex) {
                if (parent != null) {
                    parent.remove(sr);
                }
                RxJavaPlugins.onError(ex);
            }

            return sr;
        }
        分析：ScheduledRunnable与ScheduleDirectRunnable类似，但是看起来更复杂
             PARENT_INDEX，用来解决自己dispose时，从上级持有对象中删除自己
             FUTURE_INDEX，用来解决自己作为runnable，如何取消自己的问题
             THREAD_INDEX，用来解决是否在runnable线程中取消自己的问题，如果是那么future.cancel(false)

        @Override
        public void dispose() {
            if (!disposed) {
                disposed = true;
                executor.shutdownNow();
            }
        }

        public void shutdown() {
            if (!disposed) {
                disposed = true;
                executor.shutdown();
            }
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }
    }
    分析：这个类很重要呀，里面就是ScheduledExecutorService，线程池出现在这里了。其实从名称就可以看出来，它里面每次都是一个新的线程池，所以叫NewThread
    总结：每次开一个新的线程池，任务交到新的线程池中执行

    3.1 ------------------------------------------------
    [EventLoopWorker.java]
    static final class EventLoopWorker extends Scheduler.Worker {
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
                pool.release(threadWorker);
            }
        }

        @Override
        public boolean isDisposed() {
            return once.get();
        }

        @NonNull
        @Override
        public Disposable schedule(@NonNull Runnable action, long delayTime, @NonNull TimeUnit unit) {
            if (tasks.isDisposed()) {
                return EmptyDisposable.INSTANCE;
            }

            return threadWorker.scheduleActual(action, delayTime, unit, tasks);
        }
    }
    分析：通过上面的分析，这里就很简单了，其实就是创建了一个代理的Scheduler.Worker，这个worker最大的特点就是包了一层缓存
         首先从缓存CachedWorkerPool中取出来ThreadWorker（一个新线程池），然后让它执行任务
         再说到CachedWorkerPool管理缓存，里面有个定时器，每各一段时间就清理下无用的ThreadWorker（在外部调用dispose时释放掉）

拆解三：HandlerScheduler分析

    4.1 ------------------------------------------------
    [HandlerScheduler.java]
    final class HandlerScheduler extends Scheduler {
        private final Handler handler;
        private final boolean async;

        HandlerScheduler(Handler handler, boolean async) {
            this.handler = handler;
            this.async = async;
        }

        public Disposable scheduleDirect(Runnable run, long delay, TimeUnit unit) {
            run = RxJavaPlugins.onSchedule(run);
            ScheduledRunnable scheduled = new ScheduledRunnable(handler, run);
            Message message = Message.obtain(handler, scheduled);
            if (async) {
                message.setAsynchronous(true);
            }
            handler.sendMessageDelayed(message, unit.toMillis(delay));
            return scheduled;
        }

        @Override
        public Worker createWorker() {
            return new HandlerWorker(handler, async);
        }

    4.2 ------------------------------------------------
        private static final class HandlerWorker extends Worker {
            private final Handler handler;
            private final boolean async;

            private volatile boolean disposed;

            HandlerWorker(Handler handler, boolean async) {
                this.handler = handler;
                this.async = async;
            }

            public Disposable schedule(Runnable run, long delay, TimeUnit unit) {
                if (disposed) {
                    return Disposable.disposed();
                }

                run = RxJavaPlugins.onSchedule(run);

                ScheduledRunnable scheduled = new ScheduledRunnable(handler, run);

                Message message = Message.obtain(handler, scheduled);
                message.obj = this; // Used as token for batch disposal of this worker's runnables.

                if (async) {
                    message.setAsynchronous(true);
                }

                handler.sendMessageDelayed(message, unit.toMillis(delay));

                if (disposed) {
                    handler.removeCallbacks(scheduled);
                    return Disposable.disposed();
                }

                return scheduled;
            }

            @Override
            public void dispose() {
                disposed = true;
                handler.removeCallbacksAndMessages(this /* token */);
            }

            @Override
            public boolean isDisposed() {
                return disposed;
            }
        }
        分析：

    4.3 ------------------------------------------------
        private static final class ScheduledRunnable implements Runnable, Disposable {
            private final Handler handler;
            private final Runnable delegate;

            private volatile boolean disposed; // Tracked solely for isDisposed().

            ScheduledRunnable(Handler handler, Runnable delegate) {
                this.handler = handler;
                this.delegate = delegate;
            }

            @Override
            public void run() {
                try {
                    delegate.run();
                } catch (Throwable t) {
                    RxJavaPlugins.onError(t);
                }
            }

            @Override
            public void dispose() {
                handler.removeCallbacks(this);
                disposed = true;
            }

            @Override
            public boolean isDisposed() {
                return disposed;
            }
        }
    }
    分析：简单的Runnable封装带disposed功能

拆解三：NewThreadScheduler分析

    public final class NewThreadScheduler extends Scheduler {

        final ThreadFactory threadFactory;

        private static final String THREAD_NAME_PREFIX = "RxNewThreadScheduler";
        private static final RxThreadFactory THREAD_FACTORY;

        private static final String KEY_NEWTHREAD_PRIORITY = "rx3.newthread-priority";

        static {
            int priority = Math.max(Thread.MIN_PRIORITY, Math.min(Thread.MAX_PRIORITY,Integer.getInteger(KEY_NEWTHREAD_PRIORITY, Thread.NORM_PRIORITY)));
            THREAD_FACTORY = new RxThreadFactory(THREAD_NAME_PREFIX, priority);
        }

        public NewThreadScheduler() {
            this(THREAD_FACTORY);
        }

        public NewThreadScheduler(ThreadFactory threadFactory) {
            this.threadFactory = threadFactory;
        }

        @NonNull
        @Override
        public Worker createWorker() {
            return new NewThreadWorker(threadFactory);
        }
    }
    分析：说起来还是很简单的，就是每一次创建一个新的线程池。
         反观IOScheduler，如果一个NewThreadWorker被释放了，那么它会被放进expiringWorkerQueue缓存中
         1、如果有人需要NewThreadWorker，就从expiringWorkerQueue缓存中取出来
         2、IOScheduler的定时器，会定时的清理滞留在expiringWorkerQueue的worker
         感觉expiringWorkerQueue有点请外包的意思，如果活多，那么外包别走了留下来继续干；如果活一直不多，那么可以需要遣送走


拆解四：ComputationScheduler分析
    [ComputationScheduler.java]
    public final class ComputationScheduler extends Scheduler implements SchedulerMultiWorkerSupport {

        static final FixedSchedulerPool NONE;

        private static final String THREAD_NAME_PREFIX = "RxComputationThreadPool";
        static final RxThreadFactory THREAD_FACTORY;

        static final String KEY_MAX_THREADS = "rx3.computation-threads";

        static final int MAX_THREADS;

        static final PoolWorker SHUTDOWN_WORKER;

        final ThreadFactory threadFactory;
        final AtomicReference<FixedSchedulerPool> pool;

        private static final String KEY_COMPUTATION_PRIORITY = "rx3.computation-priority";

        static {
            MAX_THREADS = cap(Runtime.getRuntime().availableProcessors(), Integer.getInteger(KEY_MAX_THREADS, 0));

            SHUTDOWN_WORKER = new PoolWorker(new RxThreadFactory("RxComputationShutdown"));
            SHUTDOWN_WORKER.dispose();

            int priority = Math.max(Thread.MIN_PRIORITY, Math.min(Thread.MAX_PRIORITY,Integer.getInteger(KEY_COMPUTATION_PRIORITY, Thread.NORM_PRIORITY)));

            THREAD_FACTORY = new RxThreadFactory(THREAD_NAME_PREFIX, priority, true);

            NONE = new FixedSchedulerPool(0, THREAD_FACTORY);
            NONE.shutdown();
        }

        static int cap(int cpuCount, int paramThreads) {
            return paramThreads <= 0 || paramThreads > cpuCount ? cpuCount : paramThreads;
        }

         5.1 ------------------------------------------------
        static final class FixedSchedulerPool implements SchedulerMultiWorkerSupport {
            final int cores;

            final PoolWorker[] eventLoops;
            long n;

            FixedSchedulerPool(int maxThreads, ThreadFactory threadFactory) {
                this.cores = maxThreads;
                this.eventLoops = new PoolWorker[maxThreads];
                for (int i = 0; i < maxThreads; i++) {
                    this.eventLoops[i] = new PoolWorker(threadFactory);
                }
            }

            public PoolWorker getEventLoop() {
                int c = cores;
                if (c == 0) {
                    return SHUTDOWN_WORKER;
                }

                return eventLoops[(int)(n++ % c)];
            }

            public void shutdown() {
                for (PoolWorker w : eventLoops) {
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
                        callback.onWorker(i, new EventLoopWorker(eventLoops[index]));
                        if (++index == c) {
                            index = 0;
                        }
                    }
                    n = index;
                }
            }
        }

        public ComputationScheduler() {
            this(THREAD_FACTORY);
        }

        public ComputationScheduler(ThreadFactory threadFactory) {
            this.threadFactory = threadFactory;
            this.pool = new AtomicReference<>(NONE);
            start();
        }

        public Worker createWorker() {
            return new EventLoopWorker(pool.get().getEventLoop());
        }

        @Override
        public void createWorkers(int number, WorkerCallback callback) {
            ObjectHelper.verifyPositive(number, "number > 0 required");
            pool.get().createWorkers(number, callback);
        }

        public Disposable scheduleDirect(@NonNull Runnable run, long delay, TimeUnit unit) {
            PoolWorker w = pool.get().getEventLoop();
            return w.scheduleDirect(run, delay, unit);
        }

        public Disposable schedulePeriodicallyDirect(@NonNull Runnable run, long initialDelay, long period, TimeUnit unit) {
            PoolWorker w = pool.get().getEventLoop();
            return w.schedulePeriodicallyDirect(run, initialDelay, period, unit);
        }

        @Override
        public void start() {
            FixedSchedulerPool update = new FixedSchedulerPool(MAX_THREADS, threadFactory);
            if (!pool.compareAndSet(NONE, update)) {
                update.shutdown();
            }
        }

        @Override
        public void shutdown() {
            FixedSchedulerPool curr = pool.getAndSet(NONE);
            if (curr != NONE) {
                curr.shutdown();
            }
        }

        5.2 ------------------------------------------------
        static final class EventLoopWorker extends Scheduler.Worker {
            private final ListCompositeDisposable serial;
            private final CompositeDisposable timed;
            private final ListCompositeDisposable both;
            private final PoolWorker poolWorker;

            volatile boolean disposed;

            EventLoopWorker(PoolWorker poolWorker) {
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


拆解五：生产和消费线程的区别


拆解六：消息队列是怎么维护的（两个线程中来回串）




----------------------------------------------------------------------------------------------------
Q：周期性执行是如何做的？




----------------------------------------------------------------------------------------------------
Q：不同的调度器有什么差异？
     * 1、SingleScheduler: 每个Worker公用一个executor （虽然是每次创建一个Worker，但是底层的线程池用的是一个，这是非常常见的使用情况）
     * 2、NewThreadScheduler: 每个Worker新建一个executor
     * 3、IoScheduler：每个Worker都是从缓存池中取出来的 （Pool中创建一定数量的Worker，一个Worker一个executor，超时可以回收）
     * 4、HandlerScheduler： 每个Worker都是公用一个handler
     * 5、ImmediateThinScheduler：每个Worker都是立马执行run，里面没有线程池
     * 6、ComputationScheduler：当中是固定数量的executor （Pool中创建固定数量的Worker，一个Worker一个executor）





