package com.longtech.taskmanager.practice.observable

import com.longtech.taskmanager.practice.observer.Observer

interface ObservableSource<T> {
    fun subscribe(observer: Observer<T>)
}


