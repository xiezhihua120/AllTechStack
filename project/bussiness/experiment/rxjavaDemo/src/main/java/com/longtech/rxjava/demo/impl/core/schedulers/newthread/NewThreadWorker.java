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

package com.longtech.rxjava.demo.impl.core.schedulers.newthread;

import com.longtech.rxjava.demo.impl.core.runnable.ScheduledDirectTask;
import com.longtech.rxjava.demo.impl.core.runnable.ScheduledRunnable;
import com.longtech.rxjava.demo.impl.core.Scheduler;
import com.longtech.rxjava.demo.impl.core.dispose.Disposable;
import com.longtech.rxjava.demo.impl.core.dispose.DisposableContainer;
import com.longtech.rxjava.demo.impl.core.dispose.EmptyDisposable;
import com.longtech.rxjava.demo.impl.core.thread.SchedulerPoolFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Base class that manages a single-threaded ScheduledExecutorService as a
 * worker but doesn't perform task-tracking operations.
 *
 */
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
        if (disposed) {
            return EmptyDisposable.INSTANCE;
        }
        return scheduleActual(action, delayTime, unit, null);
    }

    /**
     * 创建一个带状态的小项目，提交到线程池中，并返回给客户
     * @param run
     * @param delayTime
     * @param unit
     * @return
     */
    public Disposable scheduleDirect(final Runnable run, long delayTime, TimeUnit unit) {
        ScheduledDirectTask task = new ScheduledDirectTask(run, true);
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
            return EmptyDisposable.INSTANCE;
        }
    }


    /**
     * 同上，但是额外的使用了一个DisableContainer来管理小项目
     * 1、DisposableContainer添加项目，在Worker中完成
     * 2、DisposableContainer删除项目，按理说可以在小项目中把状态同步出来，这里简化处理，直接把DisableContainer传递到了小项目中，由小项目来控制状态和删除
     *   (这样做的好处，应该是严格控制了DisposableContainer增加和删除Disposable的时机，保证管理上的正确性)
     *
     * @param run
     * @param delayTime
     * @param unit
     * @param parent
     * @return
     */
    public ScheduledRunnable scheduleActual(final Runnable run, long delayTime, TimeUnit unit, DisposableContainer parent) {
        Runnable decoratedRun = run;

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
        }

        return sr;
    }

    @Override
    public void dispose() {
        if (!disposed) {
            disposed = true;
            executor.shutdownNow();           // 因为每次都是给一个新线程创建了线程池，所以这里是直接关闭线程池
        }
    }

    /**
     * Shuts down the underlying executor in a non-interrupting fashion.
     */
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
