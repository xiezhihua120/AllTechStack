package com.longtech.taskmanager.runalone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.TextView
import com.longtech.taskmanager.R
import com.longtech.taskmanager.practice.core.dispose.Disposable
import com.longtech.taskmanager.practice.core.schedulers.Schedulers
import com.longtech.taskmanager.practice.core.schedulers.main.AndroidSchedulers
import com.longtech.taskmanager.practice.observable.Observable
import com.longtech.taskmanager.practice.observable.ObservableOnSubscribe
import com.longtech.taskmanager.practice.observer.ObservableEmitter
import com.longtech.taskmanager.practice.observer.Observer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    val TAG: String = MainActivity::class.java.simpleName

    var textView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.name)
        textView?.text = "taskmanager"


        Observable.create(object: ObservableOnSubscribe<String> {
            override fun subscribe(emitter: ObservableEmitter<String>) {
                for (index in 1..20) {
                    CountDownLatch(1).await(1, TimeUnit.SECONDS)
                    emitter.onNext("hello, $index second")
                }
                emitter.onComplete()
            }
        }).subscribeOn(Schedulers.IO)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<String> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: String) {
                    var thread = if (Looper.myLooper() == Looper.getMainLooper()) {"主线程"} else {"子线程"}
                    Log.d(TAG, "开始接受：$t  线程：$thread")
                }

                override fun onComplete() {
                    Log.d(TAG, "已经完成");
                }

                override fun onError() {
                    Log.d(TAG, "发生错误");
                }

            })
    }

}