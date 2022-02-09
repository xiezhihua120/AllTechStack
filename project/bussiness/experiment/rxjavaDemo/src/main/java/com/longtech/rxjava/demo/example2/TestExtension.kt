package com.longtech.rxjava.demo.example2

import android.util.Log
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object TestExtension {
    private const val TAG = "TestZip"
    fun test() {
        Observable.create<String> {
            var result1 = RetrofitExtensionClient.runWithScheduler(Schedulers.newThread()) {
                var latch = CountDownLatch(1)
                latch.await(5, TimeUnit.SECONDS)
                return@runWithScheduler "hello"
            }

            var result2 = RetrofitExtensionClient.runWithScheduler(Schedulers.io()) {
                var latch = CountDownLatch(1)
                latch.await(5, TimeUnit.SECONDS)
                return@runWithScheduler "world"
            }

            var result3 = RetrofitExtensionClient.runWithScheduler(AndroidSchedulers.mainThread()) {
                var latch = CountDownLatch(1)
                latch.await(5, TimeUnit.SECONDS)
                return@runWithScheduler "!"
            }

            it.onNext(result1 + result2 + result3)
            it.onComplete()
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnTerminate {

            }
            .subscribe(object : Observer<String> {
                override fun onSubscribe(d: Disposable?) {
                    Log.d(TAG, "开始连接");
                }

                override fun onNext(t: String?) {
                    Log.d(TAG, "处理事件:$t");
                }

                override fun onError(e: Throwable?) {
                    Log.d(TAG, "处理事件:"+ e.toString());
                }

                override fun onComplete() {
                    Log.d(TAG, "事件完成.不在接收任何事件");
                }
            })
    }
}