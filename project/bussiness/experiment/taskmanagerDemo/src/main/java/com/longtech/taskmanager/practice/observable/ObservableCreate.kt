package com.longtech.taskmanager.practice.observable

import com.longtech.taskmanager.practice.observer.ObservableEmitter
import com.longtech.taskmanager.practice.observer.Observer

/**
 * subscribe方法：把内部的source（上游）和外部的observer（下游) 进行了连接，当然都在自己内部做了observer的一层代理
 */
class ObservableCreate<T>(var source: ObservableOnSubscribe<T>) : Observable<T>() {

    override fun subscribe(observer: Observer<T>) {
        var emitter = ObservableEmitter(observer)
        observer.onSubscribe(emitter)
        this.source.subscribe(emitter)
    }

}