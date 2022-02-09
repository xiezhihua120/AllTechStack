package com.longtech.rxjava.demo.impl

import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import com.longtech.rxjava.demo.ViewProvider
import com.longtech.rxjava.demo.impl.observable.Observable
import com.longtech.rxjava.demo.impl.core.dispose.Disposable
import com.longtech.rxjava.demo.impl.observable.*
import com.longtech.rxjava.demo.impl.observer.ObservableEmitter
import com.longtech.rxjava.demo.impl.observer.Observer
import com.longtech.rxjava.demo.impl.core.schedulers.main.AndroidSchedulers
import com.longtech.rxjava.demo.impl.core.schedulers.Schedulers;
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


class Example9 : ViewProvider.ViewHolder {
    val TAG: String = Example9::class.java.simpleName
    var disposable: Disposable? = null
    
    override fun title(): String {
        return TAG
    }

    override fun onCreate(context: Context, savedInstanceState: Bundle?) {
        Observable.create(object : ObservableOnSubscribe<String> {
            override fun subscribe(emitter: ObservableEmitter<String>) {
                for (index in 1..20) {
                    CountDownLatch(1).await(1, TimeUnit.SECONDS)
                    emitter.onNext("hello, $index second")
                }
                emitter.onComplete()
            }
        }).subscribeOn(Schedulers.IO)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<String> {
                override fun onSubscribe(d: Disposable) {
                    disposable = d
                }

                override fun onNext(t: String) {
                    var thread = if (Looper.myLooper() == Looper.getMainLooper()) {"主线程"} else {"子线程"}
                    Log.d(TAG, "开始接受：$t  线程：$thread")
                }

                override fun onComplete() {
                    Log.d(TAG, "已经完成");
                }

                override fun onError() {
                    Log.d(TAG, "发生错误");
                }
            })
    }

    override fun onStart() {
       
    }

    override fun onResume() {
       
    }

    override fun onPause() {
       
    }

    override fun onDestory() {
        disposable?.dispose()
    }

    override fun onClick(view: View) {
        disposable?.dispose()
    }
}