package com.longtech.okhttp.demo.example1

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import com.longtech.okhttp.demo.ViewProvider
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSink
import java.io.File
import java.io.IOException


/**
 * 基本的GET、POST、FORM请求
 *
 */
class Example1 : ViewProvider.ViewHolder {
    val TAG: String = Example1::class.java.simpleName

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
        httpGet(view)
        httpHeader(view)
    }

    /**
     * Get请求
     */
    fun httpGet(view: View) {
        var client = OkHttpClient()
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

    /**
     * Get添加Header
     */
    fun httpHeader(view: View) {
        var client = OkHttpClient()
        var request = Request.Builder()
            .header("User-Agent", "OkHttp Headers.java")
            .addHeader("token", "2adsf3@#sd")
            .url("https://www.baidu.com")
            .build()
        var call = client.newCall(request)
        Thread() {
            call.execute().use {
                if (it.isSuccessful) {
                    var data = it.body?.string()
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(view.context, data, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(view.context, "获取失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }.start()
    }

    /**
     * Post请求
     */
    fun httpPost(view: View) {
        var client = OkHttpClient()

        var mediaType = "application/json; charset=utf-8".toMediaType()
        var postBody = "{}".toRequestBody(mediaType)
        var request = Request.Builder()
            .url("https://www.baidu.com")
            .post(postBody)
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

    /**
     * postStream请求
     */
    fun httpPostStream(view: View) {
        var client = OkHttpClient()

        var postBody = object : RequestBody() {
            override fun contentType(): MediaType? {
                return "text/x-markdown; charset=utf-8".toMediaType()
            }

            override fun writeTo(sink: BufferedSink) {
                sink.writeUtf8("numbers:\n")
                sink.writeUtf8("-------\n")
            }
        }

        var request = Request.Builder()
            .url("https://www.baidu.com")
            .post(postBody)
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

    /**
     * postFile请求
     */
    fun httpPostFile(view: View) {
        var client = OkHttpClient()

        var mediaType = "text/x-markdown; charset=utf-8".toMediaType()
        var fileBody = File("/a").asRequestBody(mediaType)

        var request = Request.Builder()
            .url("https://www.baidu.com")
            .post(fileBody)
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

    /**
     * postForm请求
     */
    fun httpPostForm(view: View) {
        var client = OkHttpClient()

        var formBody = FormBody.Builder()
            .add("search", "gg")
            .build()

        var request = Request.Builder()
            .url("https://www.baidu.com")
            .post(formBody)
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

    /**
     * postMultiPart请求
     */
    fun httpPostMultiPart(view: View) {
        var client = OkHttpClient()

        val MEDIA_TYPE_PNG = "image/png".toMediaType()

        var requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("title", "baidu")
            .addFormDataPart("image", "11.png", File("/sdcard/11").asRequestBody(MEDIA_TYPE_PNG))
            .build()

        var request = Request.Builder()
            .header("Authorization", "Client-ID ")
            .url("https://www.baidu.com")
            .post(requestBody)
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