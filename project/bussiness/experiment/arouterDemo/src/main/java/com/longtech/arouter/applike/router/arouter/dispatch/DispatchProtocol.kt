package com.longtech.arouter.applike.router.arouter.dispatch

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import androidx.core.os.bundleOf
import com.alibaba.android.arouter.launcher.ARouter
import java.lang.Exception
import java.net.URLDecoder
import java.util.*

object DispatchProtocol {
    const val SCHEME: String = "xxclient"
    const val HOST: String = "socal_app"

    /**
     * 统一跳转协议
     * 1、调试使用：adb shell am start -a $action -e url $url
     * 2、url协议：xxclient://socal_app/{path}?request={"key1": "value1"}&param1=value1, 其中request对应的参数需要做urlencode处理
     */
    fun navigation(intent: Intent?): Boolean {
        if (intent != null) {
            var bundle = intent.extras
            val action = intent.action
            if (bundle != null && TextUtils.equals(action, "android.intent.action.ALLIN_DEEPLINK")) {
                var url = bundle?.getString("url")
                if (url != null) {
                    var uri = Uri.parse(url)
                    val scheme = uri.scheme
                    val host = uri.host
                    if (TextUtils.equals(scheme, SCHEME) && TextUtils.equals(host, HOST)) {
                        val path = "/" + TextUtils.join("/", uri.pathSegments)
                        val paramsMap: Map<String, String> = parseParams(uri)
                        var params = bundleOf()
                        paramsMap.forEach {
                            if (!TextUtils.isEmpty(it.key) && !TextUtils.isEmpty(it.value)) {
                                params.putString(it.key, it.value)
                            }
                        }
                        goDeeplinkActivity(path, params)
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun goDeeplinkActivity(path: String, bundle: Bundle?) {
        ARouter.getInstance().build(path)
            .with(bundle)
            .navigation()
    }

    private fun parseParams(uri: Uri?): Map<String, String> {
        if (uri == null) {
            return HashMap()
        }
        val temp = HashMap<String, String>()
        val keys = getQueryParameterNames(uri)
        for (key in keys) {
            var value = uri.getQueryParameter(key)
            if (value != null) {
                temp[key] = value
            }
        }
        return temp
    }

    private fun getQueryParameterNames(uri: Uri): Set<String> {
        val query = uri.encodedQuery ?: return emptySet()
        val names: MutableSet<String> = LinkedHashSet()
        var start = 0
        do {
            val next = query.indexOf('&', start)
            val end = if (next == -1) query.length else next
            var separator = query.indexOf('=', start)
            if (separator > end || separator == -1) {
                separator = end
            }
            val name = query.substring(start, separator)
            try {
                names.add(URLDecoder.decode(name, "UTF-8"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            start = end + 1
        } while (start < query.length)
        return Collections.unmodifiableSet(names)
    }
}