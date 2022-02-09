package com.longtech.rxjava4retrofit.demo.example1

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import com.longtech.rxjava4retrofit.applike.demo.ViewProvider
import com.longtech.rxjava4retrofit.demo.RetrofitManager
import com.longtech.rxjava4retrofit.demo.example6.autoDispose
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.ResponseBody
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class Example1 : ViewProvider.ViewHolder {
    val TAG: String = Example1::class.java.simpleName

    var disposable = CompositeDisposable()

    override fun title(): String {
        return TAG
    }

    override fun onCreate(context: Context, savedInstanceState: Bundle?) {
        var retrofit = RetrofitManager.retrofit

        var userService = retrofit.create(UserObservableService::class.java)
        userService.requestUser()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .autoDispose(context as Activity)
            .doOnDispose {
                Log.d(TAG, "已经取消");
            }
            .doOnTerminate {
                Log.d(TAG, "已经中断");
            }
            .doFinally {
                Log.d(TAG, "全部结束");
            }
            .subscribe(object : Observer<ResponseBody> {
                override fun onSubscribe(d: Disposable?) {
                    d?.let {
                        disposable.add(it)
                    }
                    Log.d(TAG, "开始连接");
                }

                override fun onNext(t: ResponseBody?) {
                    Log.d(TAG, "onNext: " + t?.string());
                    try {
                        CountDownLatch(1).await(5, TimeUnit.SECONDS)
                    } catch (r: Throwable) {
                        //r.printStackTrace()
                    }
                }

                override fun onError(e: Throwable) {
                    Log.d(TAG, "onError: $e");
                }

                override fun onComplete() {
                    Log.d(TAG, "onComplete");
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
        disposable.dispose()
    }

}