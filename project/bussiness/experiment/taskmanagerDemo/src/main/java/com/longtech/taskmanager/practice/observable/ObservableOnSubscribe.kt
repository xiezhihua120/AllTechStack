package com.longtech.taskmanager.practice.observable

import com.longtech.taskmanager.practice.observer.ObservableEmitter

interface ObservableOnSubscribe<T> {
    fun subscribe(emitter: ObservableEmitter<T>)
}