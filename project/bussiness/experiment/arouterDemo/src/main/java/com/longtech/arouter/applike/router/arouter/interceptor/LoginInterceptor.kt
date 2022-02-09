package com.longtech.arouter.applike.router.arouter.interceptor

import android.content.Context
import android.util.Log
import com.alibaba.android.arouter.facade.callback.InterceptorCallback
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.annotation.Interceptor
import com.alibaba.android.arouter.facade.template.IInterceptor
import com.longtech.arouter.applike.router.arouter.ARouterManager


@Interceptor(priority = 1)
class LoginInterceptor : IInterceptor {
    val TAG: String = LoginInterceptor::class.java.simpleName


    override fun init(context: Context?) {
        Log.e(TAG, "first init")
    }

    override fun process(postcard: Postcard, callback: InterceptorCallback) {
        Log.e(TAG, "first process start")

        val path = postcard.path
        val bundle = postcard.extras
        var needLogin = bundle?.getBoolean(ARouterManager.NEED_LOGIN)
        if (needLogin == true) {
            val isLogin: Boolean = checkLogin()
            if (isLogin) {
                // 如果已经登录不拦截
                callback.onContinue(postcard)
            } else {
                // 如果没有登录
                doLogin()
            }
        } else {
            callback.onContinue(postcard)
        }
        Log.e(TAG, "first process end")
    }

    private fun checkLogin(): Boolean {
        // Todo: 判断是否已经登陆
        return true
    }

    private fun doLogin() {
        // Todo: 进入到登陆洁面
    }
}