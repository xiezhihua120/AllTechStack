package com.longtech.okhttp.demo.example4

import android.content.Context
import android.os.Bundle
import android.view.View
import com.longtech.okhttp.demo.ViewProvider
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * 在默认配置的基础上，单独修改个别配置，但底层共用连接池、任务分发器、配置
 */
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
        var client = OkHttpClient()

        var request = Request.Builder()
            .url("http://httpbin.org")
            .build()

        val client1 = client.newBuilder()
            .readTimeout(500, TimeUnit.SECONDS)
            .build()

        val client2 = client.newBuilder()
            .readTimeout(800, TimeUnit.SECONDS)
            .build()

        client1.newCall(request).execute()

        client2.newCall(request).execute()
    }
}