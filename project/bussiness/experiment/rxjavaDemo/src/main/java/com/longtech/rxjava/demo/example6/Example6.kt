package com.longtech.rxjava.demo.example6

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import com.longtech.rxjava.demo.ViewProvider
import com.longtech.rxjava.demo.example1.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable

class Example6 : ViewProvider.ViewHolder {
    val TAG: String = Example6::class.java.simpleName
    
    override fun title(): String {
        return TAG
    }

    override fun onCreate(context: Context, savedInstanceState: Bundle?) {

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
        // 串行发送
        Observable.concatArray(
            Observable.just(1, 2),
            Observable.just(3, 4),
            Observable.just(5, 6),
            Observable.just(7, 8),
            Observable.just(7, 8),
            Observable.just(7, 8),
            Observable.just(7, 8)
        )
            .subscribe(object : Observer<Int> {
                override fun onSubscribe(d: Disposable?) {

                }

                override fun onNext(integer: Int) {
                    Log.d(TAG, "================onNext " + integer);
                }

                override fun onError(e: Throwable?) {

                }

                override fun onComplete() {

                }
            })

        // 并行发送
        Observable.mergeArray(
            Observable.just(1, 2),
            Observable.just(3, 4),
            Observable.just(5, 6),
            Observable.just(7, 8),
            Observable.just(7, 8),
            Observable.just(7, 8),
            Observable.just(7, 8)
        )
            .subscribe(object : Observer<Int> {
                override fun onSubscribe(d: Disposable?) {

                }

                override fun onNext(integer: Int) {
                    Log.d(TAG, "================onNext " + integer);
                }

                override fun onError(e: Throwable?) {

                }

                override fun onComplete() {

                }
            })
    }
}