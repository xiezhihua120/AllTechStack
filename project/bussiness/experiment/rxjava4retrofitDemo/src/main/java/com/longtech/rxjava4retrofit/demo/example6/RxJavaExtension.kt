package com.longtech.rxjava4retrofit.demo.example6

import android.app.Activity
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable


fun <T> Observable<T>.autoDispose(activity: Activity?): Observable<T> {
    var dispose: Disposable? = null
    return this.doOnLifecycle(
        {
            dispose = it
        }, {

        }
    ).doOnNext {
        if (activity == null || activity.isFinishing) {
            dispose?.dispose()
        }
    }.filter {
        //我们用filter 和takeUntil 都可以，主要就是判断当前Activity是否销毁了
        if (activity == null || activity.isFinishing) {
            dispose?.dispose()
        }
        activity != null && !activity.isFinishing
    }
}
