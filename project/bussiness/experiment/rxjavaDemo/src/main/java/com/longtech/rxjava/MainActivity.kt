package com.longtech.rxjava

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.longtech.rxjava.demo.ViewProvider
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.lang.Exception

@Route(path = "/irxjava/main")
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
        setContentView(R.layout.rxjava_activity_main)
        scrollView = findViewById(R.id.scrollView)
        llContent = findViewById(R.id.llContent)

        ViewProvider.getHolders().forEach {
            addView(it)
            it.onCreate(this, savedInstanceState)
        }
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