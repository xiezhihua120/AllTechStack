package com.longtech.okhttp.demo.example5

import android.content.Context
import android.os.Bundle
import android.view.View
import com.longtech.okhttp.demo.ViewProvider
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * 取消一个Call
 */
class Example5 : ViewProvider.ViewHolder {
    val TAG: String = Example5::class.java.simpleName

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

    val executor = Executors.newScheduledThreadPool(1)
    val client = OkHttpClient()
    override fun onClick(view: View) {
        var request = Request.Builder()
            .url("https://www.baidu.com")
            .tag(Example5@this.hashCode())
            .build()

        var call = client.newCall(request)

        executor.schedule(object : Runnable {
            override fun run() {
                call.cancel()
            }
        }, 1, TimeUnit.SECONDS)

        try {
            var response = call.execute()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
}