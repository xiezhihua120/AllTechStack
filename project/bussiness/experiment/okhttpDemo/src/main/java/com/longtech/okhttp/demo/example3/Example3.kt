package com.longtech.okhttp.demo.example3

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import com.longtech.okhttp.demo.ViewProvider
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * 处理超时时间
 */
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
        var client = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)        // 连接请求超时
            .writeTimeout(5, TimeUnit.SECONDS)          // 写请求超时
            .readTimeout(5, TimeUnit.SECONDS)           // 读请求超时
            .callTimeout(5, TimeUnit.SECONDS)           // 全部调用的总超时
            .build()

        var request = Request.Builder()
            .url("https://www.baidu.com")
            .build()

        var call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Toast.makeText(view.context, e.toString(), Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call, response: Response) {
                var data = response.body?.string()
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(view.context, data, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}