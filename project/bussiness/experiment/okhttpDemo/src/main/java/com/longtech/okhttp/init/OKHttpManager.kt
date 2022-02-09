package com.longtech.okhttp.init

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * -------------------------------------------------------------------------------------------------
 * 1、OKHttp初始化
 *    日志拦截器   （网络拦截器）
 *    Token拦截器 （应用拦截器）
 *    埋点拦截器   （应用拦截器）
 *
 *    连接超时
 *    读取超时
 *    写入超时
 *
 *    缓存目录
 *    下载带进度支持
 *    取消操作
 *
 *    编写demo用例代码
 *
 * -------------------------------------------------------------------------------------------------
 * 2、按阶段划分配置：
 *    A、请求
 *      Token拦截器
 *
 *    B、执行
 *      连接超时
 *      写入超时
 *      读取超时
 *
 *    C、响应
 *      日志拦截器
 *      埋点拦截器
 *      写入超时
 *
 *    D、缓存
 *      缓存目录
 *
 *    E、UI
 *      下载带进度支持
 *      取消操作
 *
 *  *
 * -------------------------------------------------------------------------------------------------
 * 3、按问题场景划分：
 *    1、初始化OKHttp
 *    2、token需要携带
 *    3、开发阶段需要打印日志
 *    4、运行阶段需要上报日志
 *    5、开发阶段需要下载带进度
 *    6、运行阶段页面退出需要cancel
 *
 * -------------------------------------------------------------------------------------------------
 */
object OKHttpManager {

    lateinit var okHttpClient: OkHttpClient

    fun init(context: Context) {

        // 多域名拦截器
        val baseUrlInterceptor = object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                return chain.proceed(chain.request())
            }
        }

        // 日志打印拦截器
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BASIC

        // 日志上报拦截器
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
        .addInterceptor(baseUrlInterceptor)
        .addInterceptor(logging)
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .retryOnConnectionFailure(true)
        .connectTimeout(10, TimeUnit.SECONDS)
        //.cache(CacheUtil.getCache(UIUtil.getContext()))
        .build()

    }
}