package com.longtech.glide

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.longtech.glide.applike.glide.ViewProvider
import com.longtech.glide.demo.GlideImageLoader
import com.longtech.glide.demo.R

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
        setContentView(R.layout.glide_activity_main)
        scrollView = findViewById(R.id.scrollView)
        llContent = findViewById(R.id.llContent)

        ViewProvider.getHolders().forEach {
            addView(it)
            it.onCreate(this, savedInstanceState)
        }

        initModule()
    }

    fun initModule() {
        var image = findViewById<ImageView>(R.id.image)
        var url = "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fpic.51yuansu.com%2Fpic3%2Fcover%2F03%2F19%2F81%2F5b64393e73cec_610.jpg&refer=http%3A%2F%2Fpic.51yuansu.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1643185967&t=f526337998ab58d1076ef8707e1d6c6c"
        var gif = "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fc-ssl.duitang.com%2Fuploads%2Fitem%2F201701%2F20%2F20170120142750_2VYNQ.thumb.1000_0.gif&refer=http%3A%2F%2Fc-ssl.duitang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1643189250&t=f0a3328cd9493f46fbd426ccf20c7af3"
        var jpg = "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fpic124.nipic.com%2Ffile%2F20170319%2F14707271_231154092000_2.jpg&refer=http%3A%2F%2Fpic124.nipic.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1647510317&t=2a287879841a8f73820e5b1d60f614fc"
        var png = "https://img0.baidu.com/it/u=3576781080,361138154&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500"
        GlideImageLoader.getInstance().loadImage(this, png, image)
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