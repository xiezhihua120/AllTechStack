package com.longtech.rxjava4retrofit.demo.example3

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import com.longtech.rxjava4retrofit.applike.demo.ViewProvider
import com.longtech.rxjava4retrofit.demo.RetrofitManager
import com.longtech.rxjava4retrofit.demo.example1.UserObservableService
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.ResponseBody

class Example3 : ViewProvider.ViewHolder {
    val TAG: String = Example3::class.java.simpleName

    var disposable = CompositeDisposable()

    override fun title(): String {
        return TAG
    }

    override fun onCreate(context: Context, savedInstanceState: Bundle?) {
        var retrofit = RetrofitManager.retrofit

        var userService = retrofit.create(UserObservableService::class.java)
        var observer = userService.requestUser()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : DisposableObserver<ResponseBody>() {
                override fun onStart() {
                    Log.d(TAG, "onStart");
                }

                override fun onNext(t: ResponseBody?) {
                    Log.d(TAG, "onNext: " + t?.string());
                }

                override fun onError(e: Throwable) {
                    Log.d(TAG, "onError: $e");
                }

                override fun onComplete() {
                    Log.d(TAG, "onComplete");
                }
            })
        disposable.add(observer)
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