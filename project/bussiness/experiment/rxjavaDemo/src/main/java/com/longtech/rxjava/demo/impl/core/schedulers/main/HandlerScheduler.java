package com.longtech.rxjava.demo.impl.core.schedulers.main;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import com.longtech.rxjava.demo.impl.core.Scheduler;
import com.longtech.rxjava.demo.impl.core.dispose.Disposable;
import com.longtech.rxjava.demo.impl.core.dispose.EmptyDisposable;

import java.util.concurrent.TimeUnit;

/**
 * 管理者： Scheduler                   负责承接任务                                  public Disposable scheduleDirect(Runnable run, long delay, TimeUnit unit)
 * 负责人：Worker                       负责承接任务，并提供取消的能力                    public Disposable schedule(Runnable run, long delay, TimeUnit unit)
 * 执行团队：Handler或者Executor         执行任务，并能取消当前的所有任务                  Message message = Message.obtain(handler, scheduled);
 *                                                                                message.obj = this;
 * 总结：从整体上看，可以提交多个Runnable，每个Runnable与一个Disposable一一对应，设计十分巧妙
 */
final class HandlerScheduler extends Scheduler {
    private final Handler handler;
    private final boolean async;

    HandlerScheduler(Handler handler, boolean async) {
        this.handler = handler;
        this.async = async;
    }

    @Override
    @SuppressLint("NewApi") // Async will only be true when the API is available to call.
    public Disposable scheduleDirect(Runnable run, long delay, TimeUnit unit) {
        if (run == null) throw new NullPointerException("run == null");
        if (unit == null) throw new NullPointerException("unit == null");
        
        HandlerScheduler.ScheduledRunnable scheduled = new HandlerScheduler.ScheduledRunnable(handler, run);
        Message message = Message.obtain(handler, scheduled);
        if (async) {
            message.setAsynchronous(true);
        }
        handler.sendMessageDelayed(message, unit.toMillis(delay));
        return scheduled;
    }

    /**
     * HandlerWorker     ->     Worker(handler)
     * @return
     */
    @Override
    public Worker createWorker() {
        return new HandlerScheduler.HandlerWorker(handler, async);
    }

    private static final class HandlerWorker extends Worker {
        private final Handler handler;
        private final boolean async;

        private volatile boolean disposed;

        HandlerWorker(Handler handler, boolean async) {
            this.handler = handler;
            this.async = async;
        }

        @Override
        @SuppressLint("NewApi") // Async will only be true when the API is available to call.
        public Disposable schedule(Runnable run, long delay, TimeUnit unit) {
            if (run == null) throw new NullPointerException("run == null");
            if (unit == null) throw new NullPointerException("unit == null");

            if (disposed) {
                return EmptyDisposable.INSTANCE;
            }


            HandlerScheduler.ScheduledRunnable scheduled = new HandlerScheduler.ScheduledRunnable(handler, run);

            Message message = Message.obtain(handler, scheduled);
            message.obj = this; // Used as token for batch disposal of this worker's runnables.

            if (async) {
                message.setAsynchronous(true);
            }

            handler.sendMessageDelayed(message, unit.toMillis(delay));

            // Re-check disposed state for removing in case we were racing a call to dispose().
            if (disposed) {
                handler.removeCallbacks(scheduled);
                return EmptyDisposable.INSTANCE;
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
                //RxJavaPlugins.onError(t);
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
