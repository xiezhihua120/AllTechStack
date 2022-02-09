/*
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package com.longtech.rxjava.demo.impl.core.runnable;

import com.longtech.rxjava.demo.impl.core.dispose.Disposable;

import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;


/**
 * 总结：任务支持三种状态
 *      1、future
 *      2、FINISHED
 *      3、DISPOSED
 *
 * A、在run函数运行时，结束后设置FINISHED状态
 * B、在dispose函数中，设置DISPOSED状态，并取消任务future.cancel(true)
 * C、在isDisposed函数中，判断 FINISHED || DISPOSED
 *
 */
abstract class AbstractDirectTask
extends AtomicReference<Future<?>>
implements Disposable {

    private static final long serialVersionUID = 1811839108042568751L;

    protected final Runnable runnable;

    protected final boolean interruptOnCancel;

    protected Thread runner;

    /**
     * 使用两个FutureTask变量来保持任务状态：FINISHED、DISPOSED
     */
    protected static final FutureTask<Void> FINISHED = new FutureTask<>(new EmptyRunnable(), null);

    protected static final FutureTask<Void> DISPOSED = new FutureTask<>(new EmptyRunnable(), null);

    AbstractDirectTask(Runnable runnable, boolean interruptOnCancel) {
        this.runnable = runnable;
        this.interruptOnCancel = interruptOnCancel;
    }

    @Override
    public final void dispose() {
        Future<?> f = get();
        if (f != FINISHED && f != DISPOSED) {
            if (compareAndSet(f, DISPOSED)) {
                if (f != null) {
                    cancelFuture(f);
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
                cancelFuture(future);
                break;
            }
            if (compareAndSet(f, future)) {
                break;
            }
        }
    }

    private void cancelFuture(Future<?> future) {
        if (runner == Thread.currentThread()) {
            future.cancel(false);
        } else {
            future.cancel(interruptOnCancel);
        }
    }

    @Override
    public String toString() {
        String status;
        Future<?> f = get();
        if (f == FINISHED) {
            status = "Finished";
        } else if (f == DISPOSED) {
            status = "Disposed";
        } else {
            Thread r = runner;
            if (r != null) {
                status = "Running on " + runner;
            } else {
                status = "Waiting";
            }
        }

        return getClass().getSimpleName() + "[" + status + "]";
    }

    static final class EmptyRunnable implements Runnable {
        @Override
        public void run() { }

        @Override
        public String toString() {
            return "EmptyRunnable";
        }
    }

}
