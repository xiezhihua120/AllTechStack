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

package com.longtech.taskmanager.practice.core.runnable;

import com.longtech.taskmanager.practice.core.dispose.Disposable;
import com.longtech.taskmanager.practice.core.dispose.DisposableContainer;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * 保存了如下几个变量
 * 1、AtomicReference<Object> parent     //  有两种状态：DisposableContainer(传参)、DONE、PARENT_DISPOSED
 * 2、AtomicReference<Object> future     //  有四种状态：Future(传参)、DONE、ASYNC_DISPOSED、SYNC_DISPOSED
 * 3、AtomicReference<Object> thread     //  有两种状态：Thread.currentThread()、null
 *
 * A、在run函数中，需要设置thread；需要设置future是否完成；需要设置parent是否完成
 * B、在dispose函数中，设置future和parent是否释放
 * C、在isDisposed函数中，设置根据parent的状态来判断
 *
 *
 * 总结：支持了任务状态，进一步支持了外部的Dispose容器删除管理
 *      runnable可以认为是单纯的任务，而ScheduleRunnable可以认为是被项目管理控制过的小项目，带有状态&可控制
 */
public final class ScheduledRunnable extends AtomicReferenceArray<Object>
implements Runnable, Callable<Object>, Disposable {

    private static final long serialVersionUID = -6120223772001106981L;
    final Runnable actual;

    /**
     * 创建了四个Object对象来表示状态
     */
    static final Object PARENT_DISPOSED = new Object();
    static final Object SYNC_DISPOSED = new Object();
    static final Object ASYNC_DISPOSED = new Object();
    static final Object DONE = new Object();

    static final int PARENT_INDEX = 0;      //  保存DisposableContainer使用
    static final int FUTURE_INDEX = 1;      //  存储Runnable进入到线程池后的"引用对象future"
    static final int THREAD_INDEX = 2;      //  保存当前线程Thread.currentThread()

    /**
     * Creates a ScheduledRunnable by wrapping the given action and setting
     * up the optional parent.
     * @param actual the runnable to wrap, not-null (not verified)
     * @param parent the parent tracking container or null if none
     */
    public ScheduledRunnable(Runnable actual, DisposableContainer parent) {
        super(3);
        this.actual = actual;
        this.lazySet(0, parent);
    }

    @Override
    public Object call() {
        // Being Callable saves an allocation in ThreadPoolExecutor
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
                // Exceptions.throwIfFatal(e); nowhere to go
                throw e;
            }
        } finally {
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
            lazySet(THREAD_INDEX, null);
        }
    }

    public void setFuture(Future<?> f) {
        for (;;) {
            Object o = get(FUTURE_INDEX);
            if (o == DONE) {
                return;
            }
            if (o == SYNC_DISPOSED) {
                f.cancel(false);
                return;
            }
            if (o == ASYNC_DISPOSED) {
                f.cancel(true);
                return;
            }
            if (compareAndSet(FUTURE_INDEX, o, f)) {
                return;
            }
        }
    }

    @Override
    public void dispose() {
        for (;;) {
            Object o = get(FUTURE_INDEX);
            if (o == DONE || o == SYNC_DISPOSED || o == ASYNC_DISPOSED) {
                break;
            }
            boolean async = get(THREAD_INDEX) != Thread.currentThread();
            if (compareAndSet(FUTURE_INDEX, o, async ? ASYNC_DISPOSED : SYNC_DISPOSED)) {
                if (o != null) {
                    ((Future<?>)o).cancel(async);
                }
                break;
            }
        }

        for (;;) {
            Object o = get(PARENT_INDEX);
            if (o == DONE || o == PARENT_DISPOSED || o == null) {
                return;
            }
            if (compareAndSet(PARENT_INDEX, o, PARENT_DISPOSED)) {
                ((DisposableContainer)o).delete(this);
                return;
            }
        }
    }

    @Override
    public boolean isDisposed() {
        Object o = get(PARENT_INDEX);
        return o == PARENT_DISPOSED || o == DONE;
    }

    @Override
    public String toString() {
        String state;
        Object o = get(FUTURE_INDEX);
        if (o == DONE) {
            state = "Finished";
        } else if (o == SYNC_DISPOSED) {
            state = "Disposed(Sync)";
        } else if (o == ASYNC_DISPOSED) {
            state = "Disposed(Async)";
        } else {
            o = get(THREAD_INDEX);
            if (o == null) {
                state = "Waiting";
            } else {
                state = "Running on " + o;
            }
        }

        return getClass().getSimpleName() + "[" + state + "]";
    }
}
