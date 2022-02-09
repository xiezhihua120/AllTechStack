package com.longtech.rxjava.demo.example1

import android.os.Looper
import android.util.Log
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object TestTimeout {
    private const val TAG = "TestZip"
    fun test() {

        Observable.create<String> {
            var latch = CountDownLatch(1)
            latch.await(10, TimeUnit.SECONDS)
            it.onNext("hello")
            it.onNext("world")
            it.onComplete()

        }.timeout(5, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object: Observer<String> {
                override fun onSubscribe(d: Disposable?) {
                    Log.d(TAG, "开始连接");
                    testThread("onSubscribe")
                }

                override fun onNext(t: String?) {
                    Log.d(TAG, "处理事件:$t");
                    testThread("onNext")
                }

                override fun onError(e: Throwable?) {
                    Log.d(TAG, "处理事件:"+ e.toString());
                    testThread("onError")
                }

                override fun onComplete() {
                    Log.d(TAG, "事件完成.不在接收任何事件");
                    testThread("onComplete")
                }

                fun testThread(funName: String) {
                    if (Looper.getMainLooper() == Looper.myLooper()) {
                        Log.d(TAG, funName + ": 在主线程");
                    } else {
                        Log.d(TAG, funName + ": 在子线程");
                    }
                }
            })
    }
}