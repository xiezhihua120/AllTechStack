package com.longtech.rxjava.demo.example2

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

object RetrofitExtensionClient {


    fun <R> runWithScheduler(runScheduler: Scheduler, block: () -> R): R? {
        var result = AtomicReference<R?>()

        var latch = CountDownLatch(1)
        Observable.create<Void> {
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




}