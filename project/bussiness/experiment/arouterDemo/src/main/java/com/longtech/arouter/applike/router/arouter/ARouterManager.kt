package com.longtech.arouter.applike.router.arouter

import android.app.Application
import com.alibaba.android.arouter.BuildConfig
import com.alibaba.android.arouter.launcher.ARouter
import com.longtech.arouter.applike.router.arouter.activity.impl.ActivityARouter
import com.longtech.arouter.applike.router.arouter.activity.impl.ActivityARouterX

object ARouterManager {
    var NEED_LOGIN: String = "NEED_LOGIN"

    /**
     * 功能初始化
     */
    fun init(application: Application) {
        if (BuildConfig.DEBUG) {                // 这两行必须写在init之前，否则这些配置在init过程中将无效
            ARouter.openLog()                   // 打印日志
            ARouter.openDebug()                 // 开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
        }
        ARouter.init(application)               // 尽可能早，推荐在Application中初始化
    }

    /**
     * 相机业务
     */
    fun getCamereRouter(): ActivityARouterX {
        return ActivityARouterX
    }

    /**
     * 测试业务
     */
    fun getTestRouter(): ActivityARouter {
        return ActivityARouter
    }
}