package com.longtech.rxjava.demo.example2

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import com.longtech.rxjava.demo.ViewProvider
import com.longtech.rxjava.demo.example1.Example1
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable

class Example2 : ViewProvider.ViewHolder {
    val TAG: String = Example2::class.java.simpleName
    
    override fun title(): String {
        return TAG
    }

    override fun onCreate(context: Context, savedInstanceState: Bundle?) {
        TestExtension.test()
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
        val observable = Observable.create<Int> { emitter ->
            // 2. 在复写的subscribe（）里定义需要发送的事件
            // 通过 ObservableEmitter类对象产生事件并通知观察者
            // ObservableEmitter类介绍
            // a. 定义：事件发射器
            // b. 作用：定义需要发送的事件 & 向观察者发送事件
            emitter.onNext(1)
            emitter.onNext(2)
            emitter.onNext(3)
            emitter.onComplete() //事件完成,可以选择继续发送事件
        }

        val observer = object : Observer<Int> {
            // 通过复写对应方法来 响应 被观察者
            override fun onSubscribe(d: Disposable?) {
                Log.d(TAG, "开始连接")
            }

            // 默认最先调用复写的 onSubscribe（）
            override fun onNext(value: Int?) {
                Log.d(TAG, "处理事件$value")
            }

            override fun onError(e: Throwable?) {
                Log.d(TAG, "处理事件:" + e?.toString())
            }

            override fun onComplete() {
                Log.d(TAG, "事件完成.不在接收任何事件")
            }
        }

        observable.subscribe(observer);
    }
}