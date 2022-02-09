package com.longtech.rxjava.demo.example3

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import com.longtech.rxjava.demo.ViewProvider
import com.longtech.rxjava.demo.example2.Example2
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import java.lang.Exception

class Example3 : ViewProvider.ViewHolder {
    val TAG: String = Example3::class.java.simpleName
    
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
        Observable.create(object : ObservableOnSubscribe<Int?> {
            // 1. 创建被观察者 & 生产事件
            @Throws(Exception::class)
            override fun subscribe(emitter: ObservableEmitter<Int?>) {
                emitter.onNext(1)
                emitter.onNext(2)
                emitter.onNext(3)
                emitter.onComplete()
            }
        }).subscribe(object : Observer<Int?> {
            // 2. 通过通过订阅（subscribe）连接观察者和被观察者
            // 3. 创建观察者 & 定义响应事件的行为
            override fun onSubscribe(d: Disposable?) {
                Log.d(TAG, "开始采用subscribe连接")
            }

            // 默认最先调用复写的 onSubscribe（）
            override fun onNext(value: Int?) {
                Log.d(TAG, "处理事件$value")
            }

            override fun onError(e: Throwable?) {
                Log.d(TAG, "处理Error事件,不再接收事件")
            }

            override fun onComplete() {
                Log.d(TAG, "处理Complete事件,不再接收事件")
            }
        })
    }
}