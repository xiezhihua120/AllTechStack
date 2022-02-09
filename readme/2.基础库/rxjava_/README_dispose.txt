----------------------------------------------------------------------------------------------------
Q：链式调用是如何执行的？

ObservableEmitter                ->               ->                        ↓
        ↑

ObservableCreate                                                    CreateEmitter

        ↑
   scheduler.scheduleDirect(new SubscribeTask(parent)                       ↓

        ↑
ObservableSubscribeOn                                               SubscribeOnObserver
        ↑                                                                   ↓
ObservableObserveOn                                                 ObserveOnObserver

在Observable的链式调用过程中：ObservableObserveOn.subscribe   ->   ObservableSubscribeOn.subscribe   ->   ObservableCreate.subscribe   ->   ObservableEmitter.subscribe
当时间要发送了那么ObservableEmitter.subscribe内部：CreateEmitter.onNext   ->   SubscribeOnObserver.onNext   ->   ObserveOnObserver.onNext

总结：RxJava中不断使用代理模式，在代理的基础之上，不断添加新的功能扩展，例如：订阅线程、通知线程


----------------------------------------------------------------------------------------------------
Q：Dispose是如何构建和生效的

public final class ObservableSubscribeOn<T> extends AbstractObservableWithUpstream<T, T> {
    final Scheduler scheduler;

    public ObservableSubscribeOn(ObservableSource<T> source, Scheduler scheduler) {
        super(source);
        this.scheduler = scheduler;
    }

    @Override
    public void subscribeActual(final Observer<? super T> observer) {
        final SubscribeOnObserver<T> parent = new SubscribeOnObserver<>(observer);

        observer.onSubscribe(parent);

        parent.setDisposable(scheduler.scheduleDirect(new SubscribeTask(parent)));
    }
}

public final class ObservableObserveOn<T> extends AbstractObservableWithUpstream<T, T> {
    final Scheduler scheduler;
    final boolean delayError;
    final int bufferSize;
    public ObservableObserveOn(ObservableSource<T> source, Scheduler scheduler, boolean delayError, int bufferSize) {
        super(source);
        this.scheduler = scheduler;
        this.delayError = delayError;
        this.bufferSize = bufferSize;
    }

    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        if (scheduler instanceof TrampolineScheduler) {
            source.subscribe(observer);
        } else {
            Scheduler.Worker w = scheduler.createWorker();

            source.subscribe(new ObserveOnObserver<>(observer, w, delayError, bufferSize));
        }
    }
}

分析：1、ObservableSubscribeOn：实际上是生产线程提交了一个task，然后返回一个disposable（是如何构建和工作的）
     2、SubscribeOnObserver：原子引用了disposable
     3、SubscribeOnObserver：observer.onSubscribe(SubscribeOnObserver) 实际上是把disposeable又传递给了ObserveOnObserver
     4、ObserveOnObserver：ObserveOnObserver.onSubscribe
            @Override
             public void onSubscribe(Disposable d) {
                 if (DisposableHelper.validate(this.upstream, d)) {
                     this.upstream = d;
                     if (d instanceof QueueDisposable) {
                         @SuppressWarnings("unchecked")
                         QueueDisposable<T> qd = (QueueDisposable<T>) d;

                         int m = qd.requestFusion(QueueDisposable.ANY | QueueDisposable.BOUNDARY);

                         if (m == QueueDisposable.SYNC) {
                             sourceMode = m;
                             queue = qd;
                             done = true;
                             downstream.onSubscribe(this);
                             schedule();
                             return;
                         }
                         if (m == QueueDisposable.ASYNC) {
                             sourceMode = m;
                             queue = qd;
                             downstream.onSubscribe(this);
                             return;
                         }
                     }

                     queue = new SpscLinkedArrayQueue<>(bufferSize);

                     downstream.onSubscribe(this);
                 }
             }
     5、ObserveOnObserver：downstream.onSubscribe(this) 实际上是把自己作为disposable传递给了用户实现的disposable

     总结：用户持有的disposable实际上是 ObserveOnObserver                                   .dispose/.isDispose
          ObserveOnObserver实际上是代理了 SubscribeOnObserver                             .dispose/.isDispose
          SubscribeOnObserver实际上是代理了scheduler.scheduleDirect(disposeTask)          .dispose/.isDispose
          DisposeTask实际上是代理了EventLoopWorker                                        .dispose/.isDispose
          EventLoopWorker实际上是代理了ScheduledRunnable                                  .dispose/.isDispose

          ScheduledRunnable sr = new ScheduledRunnable(decoratedRun, parent);
          Future<?> f = executor.submit((Callable<Object>)sr);
          sr.setFuture(f);

          因此最终本质上也就是在线程池中提交了任务之后，利用Future来支持取消操作，当然也需要配置ScheduledRunnable来设置状态

----------------------------------------------------------------------------------------------------
Q: ScheduledRunnable是如何进行取消操作的

public final class ScheduledRunnable extends AtomicReferenceArray<Object>implements Runnable, Callable<Object>, Disposable {

    final Runnable actual;

    static final Object PARENT_DISPOSED = new Object();
    static final Object SYNC_DISPOSED = new Object();
    static final Object ASYNC_DISPOSED = new Object();
    static final Object DONE = new Object();

    static final int FUTURE_INDEX = 1;

    public ScheduledRunnable(Runnable actual, DisposableContainer parent) {
        super(3);
        this.actual = actual;
    }

    @Override
    public Object call() {
        run();
        return null;
    }

    @Override
    public void run() {
        try {
            try {
                actual.run();
            } catch (Throwable e) {
                RxJavaPlugins.onError(e);
            }
        } finally {
            for (;;) {
                o = get(FUTURE_INDEX);
                if (o == SYNC_DISPOSED || o == ASYNC_DISPOSED || compareAndSet(FUTURE_INDEX, o, DONE)) {
                    break;
                }
            }
        }
    }
    分析：在run()完成后，主动设置 [FUTURE_INDEX] = DONE

    public void setFuture(Future<?> f) {
        for (;;) {
            Object o = get(FUTURE_INDEX);
            if (o == DONE) {
                return;
            }
            if (compareAndSet(FUTURE_INDEX, o, f)) {
                return;
            }
        }
    }
    分析：其实是把future放到了 [FUTURE_INDEX] 原子变量中

    @Override
    public void dispose() {
        for (;;) {
            Object o = get(FUTURE_INDEX);
            if (o == DONE || o == SYNC_DISPOSED || o == ASYNC_DISPOSED) {
                break;
            }
            if (compareAndSet(FUTURE_INDEX, o, ASYNC_DISPOSED)) {
                if (o != null) {
                    ((Future<?>)o).cancel(async);
                }
                break;
            }
        }
    }
    分析：其实是设置了 [FUTURE_INDEX] 原子变量中，代表取消了；同时主动调用future.cancel(true)

    @Override
    public boolean isDisposed() {
        Object o = get(PARENT_INDEX);
        return o == PARENT_DISPOSED || o == DONE;
    }
}

----------------------------------------------------------------------------------------------------
Q: Runnable actual在ScheduledRunnable执行了dispose操作后，是如何让其不传递事件的？

    ObservableObserveOn.subscribe    ->   ObservableSubscribeOn.subscribe    ->    scheduler.scheduleDirect(new SubscribeTask(parent))


    参照：scheduler.scheduleDirect
    public Disposable scheduleDirect(@NonNull Runnable run, long delay, @NonNull TimeUnit unit) {
        final Worker w = createWorker();

        final Runnable decoratedRun = RxJavaPlugins.onSchedule(run);

        DisposeTask task = new DisposeTask(decoratedRun, w);

        w.schedule(task, delay, unit);

        return task;
    }


    static final class DisposeTask implements Disposable, Runnable, SchedulerRunnableIntrospection {

        @NonNull
        final Runnable decoratedRun;

        @NonNull
        final Worker w;

        @Nullable
        Thread runner;

        DisposeTask(@NonNull Runnable decoratedRun, @NonNull Worker w) {
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
    分析：实际上异步的连接操作，可能发生在线程池中。SubscribeTask(source.subscribe(observer))   <-   DisposeTask   <-   ScheduledRunnable   <-   由Worker放入到线程池中

        通过分析最后发现，实际的生产线程，是工作在SubscribeTask的run中的，即使是采取了dispose动作，这里也无法停止事件产生，因此需要在观察者中去分析逻辑

----------------------------------------------------------------------------------------------------
Q: Runnable actual在ScheduledRunnable执行了dispose操作后，是如何让其不传递事件的？

    static final class ObserveOnObserver<T> extends BasicIntQueueDisposable<T>
    implements Observer<T>, Runnable {

        private static final long serialVersionUID = 6576896619930983584L;
        final Observer<? super T> downstream;
        final Scheduler.Worker worker;
        final boolean delayError;
        final int bufferSize;

        SimpleQueue<T> queue;

        Disposable upstream;

        Throwable error;
        volatile boolean done;

        volatile boolean disposed;

        int sourceMode;

        boolean outputFused;

        ObserveOnObserver(Observer<? super T> actual, Scheduler.Worker worker, boolean delayError, int bufferSize) {
            this.downstream = actual;
            this.worker = worker;
            this.delayError = delayError;
            this.bufferSize = bufferSize;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                if (d instanceof QueueDisposable) {
                    @SuppressWarnings("unchecked")
                    QueueDisposable<T> qd = (QueueDisposable<T>) d;

                    int m = qd.requestFusion(QueueDisposable.ANY | QueueDisposable.BOUNDARY);

                    if (m == QueueDisposable.SYNC) {
                        sourceMode = m;
                        queue = qd;
                        done = true;
                        downstream.onSubscribe(this);
                        schedule();
                        return;
                    }
                    if (m == QueueDisposable.ASYNC) {
                        sourceMode = m;
                        queue = qd;
                        downstream.onSubscribe(this);
                        return;
                    }
                }

                queue = new SpscLinkedArrayQueue<>(bufferSize);

                downstream.onSubscribe(this);
            }
        }

        @Override
        public void onNext(T t) {
            if (done) {
                return;
            }

            if (sourceMode != QueueDisposable.ASYNC) {
                queue.offer(t);
            }
            schedule();
        }

        @Override
        public void onError(Throwable t) {
            if (done) {
                RxJavaPlugins.onError(t);
                return;
            }
            error = t;
            done = true;
            schedule();
        }

        @Override
        public void onComplete() {
            if (done) {
                return;
            }
            done = true;
            schedule();
        }

        @Override
        public void dispose() {
            if (!disposed) {
                disposed = true;
                upstream.dispose();
                worker.dispose();
                if (!outputFused && getAndIncrement() == 0) {
                    queue.clear();
                }
            }
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }

        void schedule() {
            if (getAndIncrement() == 0) {
                worker.schedule(this);
            }
        }

        void drainNormal() {
            int missed = 1;

            final SimpleQueue<T> q = queue;
            final Observer<? super T> a = downstream;

            for (;;) {
                if (checkTerminated(done, q.isEmpty(), a)) {
                    return;
                }

                for (;;) {
                    boolean d = done;
                    T v;

                    try {
                        v = q.poll();
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        disposed = true;
                        upstream.dispose();
                        q.clear();
                        a.onError(ex);
                        worker.dispose();
                        return;
                    }
                    boolean empty = v == null;

                    if (checkTerminated(d, empty, a)) {
                        return;
                    }

                    if (empty) {
                        break;
                    }

                    a.onNext(v);
                }

                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }

        void drainFused() {
            int missed = 1;

            for (;;) {
                if (disposed) {
                    return;
                }

                boolean d = done;
                Throwable ex = error;

                if (!delayError && d && ex != null) {
                    disposed = true;
                    downstream.onError(error);
                    worker.dispose();
                    return;
                }

                downstream.onNext(null);

                if (d) {
                    disposed = true;
                    ex = error;
                    if (ex != null) {
                        downstream.onError(ex);
                    } else {
                        downstream.onComplete();
                    }
                    worker.dispose();
                    return;
                }

                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }

        @Override
        public void run() {
            if (outputFused) {
                drainFused();
            } else {
                drainNormal();
            }
        }
    }

    分析：在异步情况下，事件通知逻辑在drainFused中
         1、假如生产线程done
            不影响事件通知，因为事件已经通知完成了

         2、假如生产线程dispose
            生产线程的dispose操作，都是由消费端来调用的，因此只需要关注消费端的dispose即可

         3、假如消费端dispose
            ObserveOnObserver.dispose

            @Override
            public void dispose() {
                if (!disposed) {
                    disposed = true;
                    upstream.dispose();
                    worker.dispose();
                    if (!outputFused && getAndIncrement() == 0) {
                        queue.clear();
                    }
                }
            }
            消费端在自己这一侧有记录，同时会触发生产端dispose、worker端dispose
            1、upstream.dispose：调用
            2、EventLoopWorker.dispose:   pool.release(threadWorker);
            消费端已经disposed了，就不用管生产端了，只要调用了upstream.dispose即可

         4、假如消费端done
            消费测有done变量记录即可

总结：生产端是没有办法停止的，因为在Runnable中，没法控制线程立即停止，但是可以控制消费端自己的立马dispose，同时通知生产端dispose

Demo轮子：参见project/bussiness/experiment/rxjava/src/main/java/com/longtech/rxjava/demo/impl


