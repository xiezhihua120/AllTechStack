package com.longtech.rxjava4retrofit.demo

import android.content.Context
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * -------------------------------------------------------------------------------------------------
 * Retrofit初始化
 *    Body传参配置
 *    Query传参配置
 *    baseUrl
 *    GsonConverterFactory
 *    取消操作
 *    多baseUrl
 *    Cache配置
 *    @Cache配置
 *
 * -------------------------------------------------------------------------------------------------
 */
object RetrofitManager {

    lateinit var okHttpClient: OkHttpClient
    lateinit var retrofit: Retrofit

    fun init(context: Context) {
        // 日志拦截器
        val loggingInterceptor = object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                return chain.proceed(chain.request())
            }
        }

        // 认证拦截器
        val authInterceptor = object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                return chain.proceed(chain.request())
            }
        }

        // OKHttp配置
        okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .retryOnConnectionFailure(true)
        .connectTimeout(10, TimeUnit.SECONDS)
        .addNetworkInterceptor(authInterceptor)
        //.cache(CacheUtil.getCache(UIUtil.getContext()))
        .build()

        // Retrofit配置
        retrofit = Retrofit.Builder()
            .client(OkHttpClient())
            .baseUrl("https://www.baidu.com")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
    }
}