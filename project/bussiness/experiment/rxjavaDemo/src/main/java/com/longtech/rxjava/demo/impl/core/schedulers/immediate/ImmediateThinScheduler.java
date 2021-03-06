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

package com.longtech.rxjava.demo.impl.core.schedulers.immediate;


import com.longtech.rxjava.demo.impl.core.Scheduler;
import com.longtech.rxjava.demo.impl.core.dispose.Disposable;
import com.longtech.rxjava.demo.impl.core.dispose.EmptyDisposable;

import java.util.concurrent.TimeUnit;

/**
 * ImmediateThinScheduler   1:n    ImmediateThinWorker
 */
public final class ImmediateThinScheduler extends Scheduler {

    /**
     * The singleton instance of the immediate (thin) scheduler.
     */
    public static final Scheduler INSTANCE = new ImmediateThinScheduler();

    static final Worker WORKER = new ImmediateThinWorker();

    static final Disposable DISPOSED;

    static {
        DISPOSED = EmptyDisposable.INSTANCE;
        DISPOSED.dispose();
    }

    private ImmediateThinScheduler() {
        // singleton class
    }

    @Override
    public Disposable scheduleDirect(Runnable run) {
        run.run();
        return DISPOSED;
    }

    @Override
    public Disposable scheduleDirect(Runnable run, long delay, TimeUnit unit) {
        throw new UnsupportedOperationException("This scheduler doesn't support delayed execution");
    }

    /**
     * ImmediateThinWorker     ->     Worker
     * @return
     */
    @Override
    public Worker createWorker() {
        return WORKER;
    }

    static final class ImmediateThinWorker extends Worker {

        @Override
        public void dispose() {
            // This worker is always stateless and won't track tasks
        }

        @Override
        public boolean isDisposed() {
            return false; // dispose() has no effect
        }

        @Override
        public Disposable schedule(Runnable run) {
            run.run();
            return DISPOSED;
        }

        @Override
        public Disposable schedule(Runnable run, long delay, TimeUnit unit) {
            throw new UnsupportedOperationException("This scheduler doesn't support delayed execution");
        }
    }
}
