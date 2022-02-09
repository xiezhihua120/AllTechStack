package com.longtech.rxjava.demo.impl.observable

import com.longtech.rxjava.demo.impl.observer.Observer

interface ObservableSource<T> {
    fun subscribe(observer: Observer<T>)
}


