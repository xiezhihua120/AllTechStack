package com.longtech.rxjava4retrofit.demo.example6

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.longtech.rxjava4retrofit.applike.demo.ViewProvider
import com.longtech.rxjava4retrofit.demo.RetrofitManager
import com.longtech.rxjava4retrofit.demo.example1.UserObservableService
import com.rxjava.rxlife.life
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


class Example6 : ViewProvider.ViewHolder {
    val TAG: String = Example6::class.java.simpleName
    
    override fun title(): String {
        return TAG
    }

    override fun onCreate(context: Context, savedInstanceState: Bundle?) {
        Observable.create<String> {

            var result1 = RxJavaSync.runWith(Schedulers.newThread()) {
                var latch = CountDownLatch(1)
                latch.await(5, TimeUnit.SECONDS)
                "hello"
            }

            var result2 = RxJavaSync.runWith(Schedulers.io()) {
                var latch = CountDownLatch(1)
                latch.await(5, TimeUnit.SECONDS)
                "world"
            }

            var result3 = RxJavaSync.runWith(AndroidSchedulers.mainThread()) {
                var latch = CountDownLatch(1)
                latch.await(5, TimeUnit.SECONDS)
                "!"
            }

            var result4 = RxJavaSync.runObserver {
                RetrofitManager.retrofit.create(UserObservableService::class.java).requestUser()
            }

            it.onNext(result1 + " " + result2 + result3 + " " + result4?.string()?.substring(0, 50))
            it.onComplete()

        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .life(context as LifecycleOwner)
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

    override fun onStart() {
       
    }

    override fun onResume() {
       
    }

    override fun onPause() {
       
    }

    override fun onDestory() {
       
    }

    override fun onClick(view: View) {
        
    }
}