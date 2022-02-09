package com.longtech.rxjava.demo.impl.observer

import com.longtech.rxjava.demo.impl.core.dispose.Disposable

interface Observer<T> {
    fun onSubscribe(d: Disposable)
    fun onNext(t: T)
    fun onComplete()
    fun onError()
}
