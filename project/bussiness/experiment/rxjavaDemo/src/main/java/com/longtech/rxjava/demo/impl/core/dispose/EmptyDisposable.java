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

package com.longtech.rxjava.demo.impl.core.dispose;


/**
 * Represents a stateless empty Disposable that reports being always
 * empty and disposed.
 * <p>It is also async-fuseable but empty all the time.
 * <p>Since EmptyDisposable implements QueueDisposable and is empty,
 * don't use it in tests and then signal onNext with it;
 * use Disposables.empty() instead.
 */
public enum EmptyDisposable implements Disposable {
    /**
     * Since EmptyDisposable implements QueueDisposable and is empty,
     * don't use it in tests and then signal onNext with it;
     * use Disposables.empty() instead.
     */
    INSTANCE,
    /**
     * An empty disposable that returns false for isDisposed.
     */
    NEVER
    ;

    @Override
    public void dispose() {
        // no-op
    }

    @Override
    public boolean isDisposed() {
        return this == INSTANCE;
    }

}
