package com.longtech.rxjava.demo.impl.observable

import com.longtech.rxjava.demo.impl.observer.ObservableEmitter

interface ObservableOnSubscribe<T> {
    fun subscribe(emitter: ObservableEmitter<T>)
}