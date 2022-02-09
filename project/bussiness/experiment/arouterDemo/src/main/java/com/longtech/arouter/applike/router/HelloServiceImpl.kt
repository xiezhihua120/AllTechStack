package com.longtech.arouter.applike.router

import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route
import com.longtech.arouter.applike.router.arouter.service.impl.ServiceARouter

@Route(path = ServiceARouter.SERVICE_HOME_FRAGMENT, name = "测试服务")
class HelloServiceImpl : HelloService {

    override fun sayHello(name: String): String {
        return "hello, $name"
    }

    override fun init(context: Context) {

    }
}