https://github.com/ReactiveX/RxAndroid

1、Android RxJava：基础介绍与使用
https://www.jianshu.com/p/9cb743b98b84

2、Android RxJava系列一: 基础常用详解
https://www.jianshu.com/p/143d3ae8d1c6

3、Android RxJava：基础介绍与使用
https://www.jianshu.com/p/9cb743b98b84

4、RxJava2 只看这一篇文章就够了
https://juejin.cn/post/6844903617124630535

4、Carson带你学Android：这是一篇清晰易懂的Rxjava入门教程
https://www.jianshu.com/p/a406b94f3188

5、Android-RxJava源码解析
https://www.jianshu.com/p/3d5c2109b3d8

6、Carson带你学Android：手把手带你源码分析RxJava
https://www.jianshu.com/p/e1c48a00951a

----------------------------------------------------------------------------------------------------

Q：RxJava是什么？
  关键词：可观察的、异步操作可组合、链式调用、基于事件的
  功能：基于事件流实现异步操作，类似于AsyncTask 、Handler的操作
  特点：基于事件流的链式调用、逻辑简洁、实现优雅、使用简单、随着程序逻辑的复杂性提高，依然能够保持代码简洁
  关键要素：
      被观察者（Observable） 产生事件
      观察者（Observer） 接收事件，并给出响应动作
      订阅（Subscribe） 连接 被观察者 & 观察者
      事件（Event） 被观察者 & 观察者 连接的桥梁

  总结：将观察者和被观察者拆分开，观察者可以按顺序发送事件（事件流），观察者顺序接收事件。两者通过subscribe建立关联。
       整个事件的产生与消费逻辑，可写成函数链式调用方式（例如先构建生产者、再定义生产线程、再定义消费线程、最后定义消费者，在调用的过程中就建立了两者关联）

  本质：本质上是一个观察者模式实现，通过订阅实现被观察者和观察者之间的连接，被观察者顺序发送事件，观察者顺序接受事件，发送接收是异步的。
       这个模式实现了事件的生产端、消费端的逻辑分离，同时能够实现逻辑链式调用，方便简洁

Q：RxJava解决了什么问题？
  最核心逻辑是说，将异步操作的生产端和消费端"解藕"，同时在异步回调程序"复杂度提高"的情况下，采用"链式调用"保持代码"简洁"。
  => （代码混乱）两端解藕、（回调复杂）链式简洁

----------------------------------------------------------------------------------------------------

Q: RxJava系统构成
方案：由于RxJava提供的可操作方法过多，因此只重点分析其中常用的，例如.create、.just、.delay、.map、flatMap、concat、merge、zip、doOnNext

[外观层]----------------------------------------------
Observable
ObservableSource
ObservableOnSubscribe
ObservableEmiter

Observer


[执行层]----------------------------------------------
Schedulers
SubscribeTask
Worker
DisposeTask
[执行层]----------------------------------------------
NewThreadWorker
ScheduledRunnable




----------------------------------------------------------------------------------------------------

[Observable.create逻辑分析]
1、Observable是一个范型类，继承自ObservableSource
    public abstract class Observable<T> implements ObservableSource<T>

2、ObservableSource是一个范型接口，
    public interface ObservableSource<T> {
        void subscribe(Observer<T> observer);
    }
    对于一个范型的参数，需要在类与方法的继承关系中统一

3、Observer也是一个范型接口
    public interface Observer<T> {
        void onSubscribe(Disposable d);
        void onNext(T t);
        void onError(Throwable e);
        void onComplete();
    }

    参考Retrofit中的范型定义如下，其中类的范型写在类名之后，方法的范型写在方法最前面（使用实例化时，写在方法名之后，例如ServiceMethod.parseAnnotations<Object>(retrofit, method)）
    abstract class ServiceMethod<T> {
      static <T> ServiceMethod<T> parseAnnotations(Retrofit retrofit, Method method) {
        return HttpServiceMethod.parseAnnotations(retrofit, method, requestFactory);
      }
      abstract T invoke(Object[] args);
    }

4、Observable.create函数主要用来创建一个Observable实例
    public static <T> Observable<T> create(ObservableOnSubscribe<T> source) {
        return RxJavaPlugins.onAssembly(new ObservableCreate<>(source));
    }

5、ObservableOnSubscribe是一个范型接口，主要是对Obserable与Observer连接时的行为的抽象
    public interface ObservableOnSubscribe<T> {
        void subscribe(ObservableEmitter<T> emitter) throws Throwable;
    }

6、ObservableEmitter主要是事件的发射器
    public interface ObservableEmitter<T> extends Emitter<T> {
        void setDisposable(@Nullable Disposable d);
        void setCancellable(@Nullable Cancellable c);
        boolean isDisposed();
        ObservableEmitter<T> serialize();
        boolean tryOnError(Throwable t);
    }

    public interface Emitter<T> {
        void onNext(T value);
        void onError(Throwable error);
        void onComplete();
    }

总结：通过外观接口的分析可知
     1、Observable中抽象了subscribe行为来链接Obserser，因此得到方法Observable.subscribe(observer)
     2、Observable中抽象了事件发射源，独立处理事件的参数，因此在构造方法中需要传入ObservableOnSubscribe
     3、Observable中实际构造的是ObservableCreate，还需要加以分析

----------------------------------------------------------------------------------------------------
[ObservableCreate逻辑分析]
    public final class ObservableCreate<T> extends Observable<T> {
        final ObservableOnSubscribe<T> source;

        public ObservableCreate(ObservableOnSubscribe<T> source) {
            this.source = source;
        }

        @Override
        protected void subscribeActual(Observer<? super T> observer) {
            CreateEmitter<T> parent = new CreateEmitter<>(observer);
            observer.onSubscribe(parent);

            try {
                source.subscribe(parent);
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                parent.onError(ex);
            }
        }
    }


分析：1、得到了一个Observable的实现类，专门针对create方法生成的一个ObserableCreate，后续最重要的逻辑在与调用subcribe，
        最终实际调用到ObserableCreate.subscribeActual
     2、subscribeActual的主要逻辑是构建一个CreateEmiiter
     3、obsserver调用onSubscribe
     4、source调用subscribe

     ObserableCreate | ObserableOnSubscribe | ObservableEmitter | observer
     1、代理了source
     2、执行subscribeActual

                       1、source连接emitter

                                             1、emitter代理了obsever


                                                                  1、observer被动接收消息

     本质上是source连接到了observer，但中间应该有线程切换、source合并、事件转化等逻辑要处理，所以它们之间做了很好的解耦

     ObservableCreate(source).subscribe(ObservableEmitter(observer))
     通过上面的解耦操作，可以让source和observer独立做扩展变化，实在精妙
----------------------------------------------------------------------------------------------------
    static final class CreateEmitter<T> extends AtomicReference<Disposable> implements ObservableEmitter<T>, Disposable {

        private static final long serialVersionUID = -3434801548987643227L;

        final Observer<? super T> observer;

        CreateEmitter(Observer<? super T> observer) {
            this.observer = observer;
        }

        @Override
        public void onNext(T t) {
            if (!isDisposed()) {
                observer.onNext(t);
            }
        }
    }
    分析：通过上面的拆解可以知道，ObservableEmitter内部持有Observer的引用，大致上可以认为是Observer的代理
----------------------------------------------------------------------------------------------------
RxJavaPlugins插件Hook逻辑分析

东西太多，后续可以分析一下。主要是一些插件拦截器，给外围暴露一些流程hook的接口

----------------------------------------------------------------------------------------------------
7、Observable.subscribeOn分析
    public final Observable<T> subscribeOn(Scheduler scheduler) {
        return RxJavaPlugins.onAssembly(new ObservableSubscribeOn<>(this, scheduler));
    }
    分析：在Observable.subscribeOn指定生产线程时，实际上是创建了一个代理类ObservableSubscribeOn

    7.1 ------------------------------------------------
    public final class ObservableSubscribeOn<T> {
        final Scheduler scheduler;

        public ObservableSubscribeOn(ObservableSource<T> source, Scheduler scheduler) {
            super(source);
            this.scheduler = scheduler;
        }

        @Override
        public void subscribeActual(final Observer<? super T> observer) {
            final SubscribeOnObserver<T> parent = new SubscribeOnObserver<>(observer);

            observer.onSubscribe(parent);

            parent.setDisposable(scheduler.scheduleDirect(new SubscribeTask(source，parent)));
        }
    }
    分析：代理类ObservableSubscribeOn核心逻辑在subscribeActual逻辑中了，在这个逻辑中
        1、创建了一个observer的代理对象SubscribeOnObserver
        2、这里采用了schedule.scheduleDirect(SubscribeTask)的方式开始执行任务，逻辑不详，需要深入分析
          (故意在SubscribeTask中加入了source参数，原来source和parent的连接在ObservableCreate中，现
          在是通过层层代理放入到了SubscribeTask中了，而SubscribeTask是一个Runnable)

    7.2 ------------------------------------------------
    static final class SubscribeOnObserver<T> implements Observer<T>, Disposable {

        private static final long serialVersionUID = 8094547886072529208L;
        final Observer<? super T> downstream;

        final AtomicReference<Disposable> upstream;

        SubscribeOnObserver(Observer<? super T> downstream) {
            this.downstream = downstream;
            this.upstream = new AtomicReference<>();
        }

        @Override
        public void onSubscribe(Disposable d) {
            DisposableHelper.setOnce(this.upstream, d);
        }

        @Override
        public void onNext(T t) {
            downstream.onNext(t);
        }
    }
    分析：代理了Observer，同时保存了Disposable，且可以取消

    7.3 ------------------------------------------------
    final class SubscribeTask implements Runnable {
        private final SubscribeOnObserver<T> parent;

        SubscribeTask(SubscribeOnObserver<T> parent) {
            this.parent = parent;
        }

        @Override
        public void run() {
            source.subscribe(parent);
        }
    }

    分析：SubscribeTask代理了SubscribeOnObserver，SubscribeOnObserver代理了observer

    7.4 ------------------------------------------------
    Disposable dispose = scheduler.scheduleDirect(new SubscribeTask(parent))
    parent.setDisposable(dispose);
    分析：主要是scheduler分发了任务，并开始执行；其次是把执行的任务设置成可dispose的任务

    7.5 ------------------------------------------------
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
    分析：scheduleDirect代表立马执行，首先创建一个Worker，其次取出runnable，将Worker和runnbale组成DisposeTask
         接着开始执行：Worker.schedule(DisposeTask)

    7.6 ------------------------------------------------
        public abstract static class Worker implements Disposable {

            public Disposable schedule(Runnable run) {
                return schedule(run, 0L, TimeUnit.NANOSECONDS);
            }

            public abstract Disposable schedule(Runnable run, long delay, TimeUnit unit);

        }
        分析：Worker需要每一个调度器自己去实现

    7.7 ------------------------------------------------
        static final class DisposeTask implements Disposable, Runnable, SchedulerRunnableIntrospection {

            @NonNull
            final Runnable decoratedRun;

            @NonNull
            final Worker w;

            @Nullable
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
        分析：DisposeTask只是对runnable的一个封装，同时封装了外围worker的dispose的操作和状态
             1、runnable只是无脑执行
             2、worker中是可以打断取消的，但是状态也需要传递给DisposeTask，同时DisposeTask也可以进行取消操作
               因此虽然是在worker中维护dispose，但这里交给了DisposeTask来维护
    }

    7.8 ------------------------------------------------
    public static Scheduler io() {
        return RxJavaPlugins.onIoScheduler(IO);
    }

    IO = RxJavaPlugins.initIoScheduler(new IOTask());

    static final class IOTask implements Supplier<Scheduler> {
        @Override
        public Scheduler get() {
            return IoHolder.DEFAULT;
        }
    }

    static final class IoHolder {
        static final Scheduler DEFAULT = new IoScheduler();
    }


    7.9 ------------------------------------------------
    public final class IoScheduler extends Scheduler {

        public IoScheduler() {
            this(WORKER_THREAD_FACTORY);
        }

        public IoScheduler(ThreadFactory threadFactory) {
            this.threadFactory = threadFactory;
            this.pool = new AtomicReference<>(NONE);
            start();
        }

        @Override
        public void start() {
            CachedWorkerPool update = new CachedWorkerPool(KEEP_ALIVE_TIME, KEEP_ALIVE_UNIT, threadFactory);
            if (!pool.compareAndSet(NONE, update)) {
                update.shutdown();
            }
        }

        @Override
        public void shutdown() {
            CachedWorkerPool curr = pool.getAndSet(NONE);
            if (curr != NONE) {
                curr.shutdown();
            }
        }

        @NonNull
        @Override
        public Worker createWorker() {
            return new EventLoopWorker(pool.get());
        }

        public int size() {
            return pool.get().allWorkers.size();
        }

        分析：需要回溯代码进行分析，最重要的是 w.schedule(task, delay, unit)，所以重点分析下面的代码

    7.10 ------------------------------------------------
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
            public Disposable schedule(Runnable action, long delayTime, TimeUnit unit) {
                return threadWorker.scheduleActual(action, delayTime, unit, tasks);
            }
        }
        分析：首先是构造函数中，从线程池中取出了一个ThreadWorker
             其次利用ThreadWorker来执行任务

    7.11 ------------------------------------------------
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
        分析：ThreadWorker实际上是一个简单子类，所以干活的是NewThreadWorker.scheduleActual(action, delayTime, unit, tasks);
    }

    7.11 ------------------------------------------------
    public class NewThreadWorker extends Scheduler.Worker implements Disposable {
        private final ScheduledExecutorService executor;

        volatile boolean disposed;

        public NewThreadWorker(ThreadFactory threadFactory) {
            executor = SchedulerPoolFactory.create(threadFactory);
        }

        @Override
        public Disposable schedule(final Runnable run) {
            return schedule(run, 0, null);
        }

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

        public ScheduledRunnable scheduleActual(final Runnable run, long delayTime, TimeUnit unit, @Nullable DisposableContainer parent) {
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
        分析：首先获取到Runnable，生成一个ScheduleRunnable，可以先去分析一下这个类（本质上是一个可以取消的Runnable）
             然后调用executor.submit执行，得到一个Future

             所以最终的执行情况是 executor.submit(ScheduleRunnable(DisposeTask(SubscribeTask(source, parent), Worker)))

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

    7.11 ------------------------------------------------
    public final class ScheduledRunnable extends AtomicReferenceArray<Object>
    implements Runnable, Callable<Object>, Disposable {

        private static final long serialVersionUID = -6120223772001106981L;
        final Runnable actual;

        public ScheduledRunnable(Runnable actual, DisposableContainer parent) {
            super(3);
            this.actual = actual;
            this.lazySet(0, parent);
        }

        @Override
        public Object call() {
            run();
            return null;
        }

        @Override
        public void run() {
            lazySet(THREAD_INDEX, Thread.currentThread());
            try {
                try {
                    actual.run();
                } catch (Throwable e) {
                    RxJavaPlugins.onError(e);
                }
            } finally {
                lazySet(THREAD_INDEX, null);
                Object o = get(PARENT_INDEX);
                if (o != PARENT_DISPOSED && compareAndSet(PARENT_INDEX, o, DONE) && o != null) {
                    ((DisposableContainer)o).delete(this);
                }

                for (;;) {
                    o = get(FUTURE_INDEX);
                    if (o == SYNC_DISPOSED || o == ASYNC_DISPOSED || compareAndSet(FUTURE_INDEX, o, DONE)) {
                        break;
                    }
                }
            }
        }
        // 分析：本质上是一个Runnable，但是也可以cancle和dispose，后续可以深入研究一下
                1、进入run()中执行，并捕获异常
                2、从DisposableContainer中删除自己
                3、适配了FutureTask操作
    }

----------------------------------------------------------------------------------------------------
8、Observable.observeOn分析

    8.1 ------------------------------------------------
    public final Observable<T> observeOn(Scheduler scheduler) {
        return observeOn(scheduler, false, bufferSize());
    }

    public final Observable<T> observeOn(Scheduler scheduler, boolean delayError, int bufferSize) {
        return RxJavaPlugins.onAssembly(new ObservableObserveOn<>(this, scheduler, delayError, bufferSize));
    }

    8.1 ------------------------------------------------
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
        分析：通过之前的逻辑分析可知，被代理的Observable中最重要的逻辑还是连接，即subscribeActual方法
             这里是通过Scheduler来创建的一个Worker
             然后构建了ObserveOnObserver代理（其中包含observer和woker）
             到最后是直接让source和observer连接了

    8.3 ------------------------------------------------
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
            分析：这是Observable直接调用过来的逻辑，传递给observer即可，后续在分析一下详细逻辑

    8.4 ------------------------------------------------
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
            分析：onNext必然是在source中主动调用的，这里出发了schedule

    8.5 ------------------------------------------------
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
            分析：都是在source中主动调用的，他们触发schedule操作

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

    8.6 ------------------------------------------------
            void schedule() {
                if (getAndIncrement() == 0) {
                    worker.schedule(this);
                }
            }
            分析：实际上是把自己当成Runnable交给worker来执行了，所以后续重点在run()函数中，该函数主要执行drainNormal逻辑

    8.7 ------------------------------------------------
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

            @Override
            public void run() {
                if (outputFused) {
                    drainFused();
                } else {
                    drainNormal();
                }
            }

        }
    }

    总结：对于Observable.observeOn而来，其实是返回了一个ObservableObserveOn对象，在它的subscribeActual方法中
         1、创建了一个ObserveOnObserver，里面包含一个worker
         2、source.subscribe(ObserveOnObserver(worker, observer))
         3、这里实际上是用了一个异步的事件队列queue，来接收事件
         4、接收到事件后，交给worker来执行runnable
         5、在run()函数中实际上是给observer发送事件（observer.onNext）

    对比：ObservableScribeOn在于：构建了一个worker，来执行SubscribeTask，只需要提交到线程池中即可
         ObservableObserveOn在于：构建了一个worker和一个queue，queue用来缓存事件，然后提交到worker中执行.onNext
         线程的切换写得实在精妙呀！

----------------------------------------------------------------------------------------------------
9、DisposableHelper分析



----------------------------------------------------------------------------------------------------
10、Dispose是如何构建和生效的