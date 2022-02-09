package com.longtech.rxjava.demo.example1

import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import com.longtech.rxjava.MainActivity
import com.longtech.rxjava.demo.ViewProvider
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

class Example1 : ViewProvider.ViewHolder {
    val TAG: String = Example1::class.java.simpleName

    override fun title(): String {
        return TAG
    }

    override fun onCreate(context: Context, savedInstanceState: Bundle?) {
        var disposable = CompositeDisposable()
        Observable.create<String> {
            it.onNext("hello")
            it.onNext("world")
            it.onComplete()
        }.subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnTerminate {

            }
            .subscribe(object : Observer<String> {
                override fun onSubscribe(d: Disposable?) {
                    if (d != null) {
                        disposable.addAll(d)
                    }
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
        disposable.dispose()
    }

    override fun onStart() {
        TestZip.test()
        TestCombineLatest.test()
        TestReduce.test()
        TestDelay.test()
        TestDoOnEach.test()
        TestDoAfterNext.test()
        TestRepeatWhen.test()
        TestTimeout.test()
        TestCompetition.test()
        TestFilter.test()
        TestInterval.test()
        TestFlatMap.test()
    }

    override fun onResume() {
       
    }

    override fun onPause() {
       
    }

    override fun onDestory() {
       
    }

    override fun onClick(view: View) {
        Observable.just("one", "two", "three", "four", "five")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object: Observer<String> {
                override fun onSubscribe(d: Disposable?) {
                    Log.d(TAG, "开始连接");
                    testThread("onSubscribe")
                }

                override fun onNext(t: String?) {
                    Log.d(TAG, "处理事件:"+ t   );
                    testThread("onNext")
                }

                override fun onError(e: Throwable?) {
                    Log.d(TAG, "处理事件:"+ e.toString()   );
                    testThread("onError")
                }

                override fun onComplete() {
                    Log.d(TAG, "事件完成.不在接收任何事件"  );
                    testThread("onComplete")
                }

                fun testThread(funName: String) {
                    if (view.context.mainLooper == Looper.myLooper()) {
                        Log.d(TAG, funName + ": 在主线程");
                    } else {
                        Log.d(TAG, funName + ": 在子线程");
                    }
                }
            });
    }
}