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

package com.longtech.rxjava.demo.impl.core.schedulers.single;

import com.longtech.rxjava.demo.impl.core.thread.RxThreadFactory;
import com.longtech.rxjava.demo.impl.core.runnable.ScheduledDirectTask;
import com.longtech.rxjava.demo.impl.core.runnable.ScheduledRunnable;
import com.longtech.rxjava.demo.impl.core.Scheduler;
import com.longtech.rxjava.demo.impl.core.dispose.CompositeDisposable;
import com.longtech.rxjava.demo.impl.core.dispose.Disposable;
import com.longtech.rxjava.demo.impl.core.dispose.EmptyDisposable;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;


/**
 * SingleScheduler   1:n  ScheduledWorker  ->   ScheduledThreadPoolExecutor
 */
public final class SingleScheduler extends Scheduler {

    final ThreadFactory threadFactory;
    final AtomicReference<ScheduledExecutorService> executor = new AtomicReference<>();

    /** The name of the system property for setting the thread priority for this Scheduler. */
    private static final String KEY_SINGLE_PRIORITY = "rx3.single-priority";

    private static final String THREAD_NAME_PREFIX = "RxSingleScheduler";

    static final RxThreadFactory SINGLE_THREAD_FACTORY;

    static final ScheduledExecutorService SHUTDOWN;
    static {
        SHUTDOWN = Executors.newScheduledThreadPool(0);
        SHUTDOWN.shutdown();

        int priority = Math.max(Thread.MIN_PRIORITY, Math.min(Thread.MAX_PRIORITY,
                Integer.getInteger(KEY_SINGLE_PRIORITY, Thread.NORM_PRIORITY)));

        SINGLE_THREAD_FACTORY = new RxThreadFactory(THREAD_NAME_PREFIX, priority, true);
    }

    public SingleScheduler() {
        this(SINGLE_THREAD_FACTORY);
    }

    /**
     * Constructs a SingleScheduler with the given ThreadFactory and prepares the
     * single scheduler thread.
     * @param threadFactory thread factory to use for creating worker threads. Note that this takes precedence over any
     *                      system properties for configuring new thread creation. Cannot be null.
     */
    public SingleScheduler(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        executor.lazySet(createExecutor(threadFactory));
    }

    static ScheduledExecutorService createExecutor(ThreadFactory threadFactory) {
        final ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1, threadFactory);
        exec.setRemoveOnCancelPolicy(true);
        return exec;
    }

    @Override
    public void start() {
        ScheduledExecutorService next = null;
        for (;;) {
            ScheduledExecutorService current = executor.get();
            if (current != SHUTDOWN) {
                if (next != null) {
                    next.shutdown();
                }
                return;
            }
            if (next == null) {
                next = createExecutor(threadFactory);
            }
            if (executor.compareAndSet(current, next)) {
                return;
            }

        }
    }

    @Override
    public void shutdown() {
        ScheduledExecutorService current =  executor.getAndSet(SHUTDOWN);
        if (current != SHUTDOWN) {
            current.shutdownNow();
        }
    }

    /**
     * 虽然是每次创建一个Worker，但是底层的线程池用的是一个，这是非常常见的使用情况
     * 1、SingleScheduler: 每个Worker公用一个executor
     * 2、NewThreadScheduler: 每个Worker新建一个executor
     * 3、IoScheduler：每个Worker都是从缓存池中取出来的 （Pool中创建一定数量的Worker，一个Worker一个executor，超时可以回收）
     * 4、HandlerScheduler： 每个Worker都是公用一个handler
     * 5、ImmediateThinScheduler：每个Worker都是立马执行run，里面没有线程池
     * 6、ComputationScheduler：当中是固定数量的executor （Pool中创建固定数量的Worker，一个Worker一个executor）
     * @return
     */
    @Override
    public Worker createWorker() {
        return new ScheduledWorker(executor.get());
    }

    
    @Override
    public Disposable scheduleDirect(Runnable run, long delay, TimeUnit unit) {
        ScheduledDirectTask task = new ScheduledDirectTask(run, true);
        try {
            Future<?> f;
            if (delay <= 0L) {
                f = executor.get().submit(task);
            } else {
                f = executor.get().schedule(task, delay, unit);
            }
            task.setFuture(f);
            return task;
        } catch (RejectedExecutionException ex) {
            return EmptyDisposable.INSTANCE;
        }
    }


    static final class ScheduledWorker extends Scheduler.Worker {

        final ScheduledExecutorService executor;

        final CompositeDisposable tasks;

        volatile boolean disposed;

        ScheduledWorker(ScheduledExecutorService executor) {
            this.executor = executor;
            this.tasks = new CompositeDisposable();
        }

        
        @Override
        public Disposable schedule( Runnable run, long delay,  TimeUnit unit) {
            if (disposed) {
                return EmptyDisposable.INSTANCE;
            }

            Runnable decoratedRun = run;

            ScheduledRunnable sr = new ScheduledRunnable(decoratedRun, tasks);
            tasks.add(sr);

            try {
                Future<?> f;
                if (delay <= 0L) {
                    f = executor.submit((Callable<Object>)sr);
                } else {
                    f = executor.schedule((Callable<Object>)sr, delay, unit);
                }

                sr.setFuture(f);
            } catch (RejectedExecutionException ex) {
                dispose();
                return EmptyDisposable.INSTANCE;
            }

            return sr;
        }

        @Override
        public void dispose() {
            if (!disposed) {
                disposed = true;
                tasks.dispose();
            }
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }
    }
}
