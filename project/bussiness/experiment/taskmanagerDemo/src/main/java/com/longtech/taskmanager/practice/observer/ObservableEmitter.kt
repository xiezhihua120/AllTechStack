package com.longtech.taskmanager.practice.observer

import com.longtech.taskmanager.practice.core.dispose.Disposable
import java.util.concurrent.atomic.AtomicReference

class ObservableEmitter<T>(var observer: Observer<T>): AtomicReference<Disposable>(), Disposable {

    @Volatile var disposed = false

    fun onNext(t: T) {
        if (isDisposed()) return
        observer.onNext(t)
    }

    fun onComplete() {
        if (isDisposed()) return
        observer.onComplete()
    }

    fun onError() {
        if (isDisposed()) return
        observer.onError()
    }

    fun setDisposable(d: Disposable) {
        this.set(d)
    }

    override fun dispose() {
        disposed = true
    }

    override fun isDisposed(): Boolean {
        return disposed
    }
}