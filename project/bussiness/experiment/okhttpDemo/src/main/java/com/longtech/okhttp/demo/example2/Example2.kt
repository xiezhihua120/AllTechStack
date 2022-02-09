package com.longtech.okhttp.demo.example2

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import com.longtech.okhttp.demo.ViewProvider
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.io.File
import java.io.IOException

/**
 * 缓存Cache处理
 */
class Example2 : ViewProvider.ViewHolder {
    val TAG: String = Example2::class.java.simpleName

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
            .cache(Cache(
                directory = File("/aa"),
                maxSize = 10L * 1024L * 1024L
            ))
            .build()

        var request = Request.Builder()
            .header("token", "123")
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