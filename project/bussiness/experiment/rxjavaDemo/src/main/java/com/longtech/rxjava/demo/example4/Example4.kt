package com.longtech.rxjava.demo.example4

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import com.longtech.rxjava.demo.ViewProvider
import com.longtech.rxjava.demo.example3.Example3
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import java.lang.Exception

class Example4 : ViewProvider.ViewHolder {
    val TAG: String = Example4::class.java.simpleName
    
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
        // 主要在观察者 Observer中 实现
        val observer: Observer<Int?> = object : Observer<Int?> {
            // 1. 定义Disposable类变量
            private var mDisposable: Disposable? = null
            override fun onSubscribe(d: Disposable?) {
                Log.d(TAG, "开始采用subscribe连接")
                // 2. 对Disposable类变量赋值
                mDisposable = d
            }

            override fun onNext(value: Int?) {
                Log.d(TAG, "对Next事件" + value + "作出响应")
                if (value == 2) {
                    // 设置在接收到第二个事件后切断观察者和被观察者的连接
                    mDisposable!!.dispose()
                    Log.d(TAG, "已经中段了连接：" + mDisposable!!.isDisposed)
                }
            }

            override fun onError(e: Throwable?) {
                Log.d(TAG, "处理Error事件") //无法收到Error事件了
            }

            override fun onComplete() {
                Log.d(TAG, "处理Complete事件") //无法收到Complete事件了
            }
        }

        Observable.create(object : ObservableOnSubscribe<Int?> {
            // 1. 创建被观察者 & 生产事件
            @Throws(Exception::class)
            override fun subscribe(emitter: ObservableEmitter<Int?>) {
                emitter.onNext(1)
                emitter.onNext(2)
                emitter.onNext(3)
                emitter.onComplete()
            }
        }).subscribe(observer)
    }
}