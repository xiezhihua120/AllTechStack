package com.longtech.rxjava4retrofit

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.alibaba.android.arouter.facade.annotation.Route
import com.longtech.rxjava4retrofit.applike.demo.ViewProvider
import com.longtech.rxjava4retrofit.demo.RetrofitManager
import com.longtech.rxjava4retrofit.demo.example1.UserObservableService
import com.longtech.rxjava4retrofit.demo.example6.RxJavaSync
import com.rxjava.rxlife.life
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


@Route(path = "/okhttp/main")
class MainActivity : AppCompatActivity() {
    val TAG: String = MainActivity::class.java.simpleName

    var scrollView: ScrollView? = null
    var llContent: LinearLayout? = null

    fun dp2px(context: Context, value: Float): Int {
        return (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.resources.displayMetrics) + 0.5f).toInt()
    }

    fun addView(holder: ViewProvider.ViewHolder) {
        var textView = TextView(this)
        textView.textSize = dp2px(this, 8f).toFloat()
        textView.text = textView.hashCode().toString()
        textView.setBackgroundColor(Color.GRAY)
        llContent!!.addView(textView)

        val lp: ViewGroup.LayoutParams = textView.getLayoutParams()
        if (lp != null) {
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT
            lp.height = dp2px(this, 40f)
        }

        textView.text = holder.title()
        textView.setOnClickListener {
            holder.onClick(textView)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rxjava4retrofit_activity_main)
        scrollView = findViewById(R.id.scrollView)
        llContent = findViewById(R.id.llContent)

        RetrofitManager.init(this)

        ViewProvider.getHolders().forEach {
            addView(it)
            it.onCreate(this, savedInstanceState)
        }




        Observable.create<String> {
            var result4 = RxJavaSync.runObserver {
                return@runObserver RetrofitManager.retrofit.create(UserObservableService::class.java).requestUser()
            }

            it.onNext(result4?.string()?.substring(0, 50))
            it.onComplete()

        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .life(this)
            .subscribe(object : Observer<String> {
                override fun onSubscribe(d: Disposable?) {
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

    }

    override fun onStart() {
        super.onStart()
        ViewProvider.getHolders().forEach {
            it.onStart()
        }
    }

    override fun onResume() {
        super.onResume()
        ViewProvider.getHolders().forEach {
            it.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        ViewProvider.getHolders().forEach {
            it.onPause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ViewProvider.getHolders().forEach {
            it.onDestory()
        }
    }
}