package com.longtech.rxjava4retrofit.demo.example4.mvp

import android.content.Context
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

import android.content.Intent
import android.util.Log
import com.longtech.rxjava4retrofit.demo.RetrofitManager
import com.longtech.rxjava4retrofit.demo.example1.UserObservableService

import com.longtech.rxjava4retrofit.demo.example4.mvp.base.IPresenter
import com.longtech.rxjava4retrofit.demo.example4.mvp.base.IView
import io.reactivex.rxjava3.observers.DisposableObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.ResponseBody

class BookPresenter(var context: Context) : IPresenter {
    val TAG: String = BookPresenter::class.java.simpleName

    private var mBookView: BookView? = null
    private var mBook: Book? = null
    override fun onCreate() {

    }

    override fun onStart() {

    }
    override fun onStop() {

    }

    override fun pause() {

    }
    override fun attachView(view: IView) {
        mBookView = view as BookView
    }

    override fun attachIncomingIntent(intent: Intent?) {

    }

    fun getSearchBooks(name: String?, tag: String?, start: Int, count: Int) {
        var retrofit = RetrofitManager.retrofit

        var userService = retrofit.create(UserObservableService::class.java)
        userService.requestUser()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : DisposableObserver<ResponseBody>() {
                override fun onStart() {
                    Log.d(TAG, "onStart");
                }

                override fun onNext(t: ResponseBody?) {
                    var ret = t?.string()
                    Log.d(TAG, "onNext: $ret");
                    mBookView?.onSuccess(Book("xiezh", ret))
                }

                override fun onError(e: Throwable) {
                    Log.d(TAG, "onError: $e");
                    mBookView?.onError(e.toString())
                }

                override fun onComplete() {
                    Log.d(TAG, "onComplete");
                }
            })
    }
}