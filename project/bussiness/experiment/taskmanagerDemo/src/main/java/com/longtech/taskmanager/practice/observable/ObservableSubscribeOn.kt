package com.longtech.taskmanager.practice.observable

import com.longtech.taskmanager.practice.core.Scheduler
import com.longtech.taskmanager.practice.core.dispose.Disposable
import com.longtech.taskmanager.practice.core.dispose.DisposableHelper
import com.longtech.taskmanager.practice.observer.Observer
import java.util.concurrent.atomic.AtomicReference

class ObservableSubscribeOn<T>(
    var source: ObservableSource<T>,
    var scheduler: Scheduler,
): Observable<T>() {

    /**
     *  通知方向：observer    <-    SubscribeOnObserver    <-   SubscribeTask   <-    scheduler.scheduleDirect(SubscribeTask)
     *  释放方向：observer    ->    SubscribeOnObserver    ->    释放线程池 + 释放上级
     */

    override fun subscribe(observer: Observer<T>) {
        // 构造函数：入参进入的是下级
        var parent = SubscribeOnObserver(observer)
        // onSubscribe：入参进入的是上级
        observer.onSubscribe(parent)

        // 生产者、消费者两个位置，针对每一次连接都会新创建一个Worker
        parent.setDisposable(scheduler.scheduleDirect(SubscribeTask(parent)))
    }

    /**
     * dispose操作一定来自于下级observer，最终是客户操作的dispose
     */
    class SubscribeOnObserver<T>(
        //  下一级观察者
        var downstream: Observer<T>,
    ): Observer<T>, AtomicReference<Disposable>(), Disposable {


        // 上一级观察者
        val upstream: AtomicReference<Disposable> = AtomicReference<Disposable>()

        override fun onNext(t: T) {
            downstream.onNext(t)
        }

        override fun onSubscribe(d: Disposable) {
            DisposableHelper.setOnce(upstream, d)
        }

        override fun onComplete() {
            downstream.onComplete()
        }

        override fun onError() {
            downstream.onError()
        }

        fun setDisposable(d: Disposable) {
            // 持有的线程池中的任务Dispose
            DisposableHelper.setOnce(this, d)
        }

        override fun dispose() {
            // 这里并没有关闭下级观察者，是因为只有客户触发dispose，因此每次的dispose都是下级发起的，所以不用管下级
            // dispose的传递路径：客户（也是下级） -> 下级观察者 -> 上级观察者
            DisposableHelper.dispose(this)
            DisposableHelper.dispose(upstream)
        }

        override fun isDisposed(): Boolean {
            return DisposableHelper.isDisposed(this.get())
        }
    }

    inner class SubscribeTask(
        var parent: SubscribeOnObserver<T>,
    ) : Runnable {
        override fun run() {
            source.subscribe(parent)
        }

    }
}