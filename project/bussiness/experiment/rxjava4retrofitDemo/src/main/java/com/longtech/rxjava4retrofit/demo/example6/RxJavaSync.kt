package com.longtech.rxjava4retrofit.demo.example6

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

object RxJavaSync {

    /**
     * 在指定的Scheduler中运行代码块，同步返回结果
     */
    fun <R> runWith(runScheduler: Scheduler, block: () -> R): R? {
        var result = AtomicReference<R>()

        var latch = CountDownLatch(1)
        Observable.create<R> {
            var ret = block.invoke()
            result.set(ret)
            latch.countDown()
        }.subscribeOn(runScheduler)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
        if (latch.count != 0L) {
            latch.await()
        }

        return result.get()
    }

    /**
     * 在Schedulers.io中运行网络请求，同步返回结果
     */
    fun <R> runObserver(block: () -> Observable<R>): R? {
        var result = AtomicReference<R>()
        var error = AtomicReference<Throwable>()

        var latch = CountDownLatch(1)
        block.invoke().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<R> {
                override fun onSubscribe(d: Disposable?) {

                }

                override fun onNext(t: R) {
                    result.set(t)
                    latch.countDown()
                }

                override fun onError(e: Throwable) {
                    error.set(e)
                    latch.countDown()
                }

                override fun onComplete() {

                }
            })
        if (latch.count != 0L) {
            latch.await()
        }

        if (error.get() != null) {
            throw error.get()
        }
        return result.get()
    }

}