package com.longtech.arouter.applike.router

import com.alibaba.android.arouter.facade.template.IProvider

interface HelloService: IProvider {
    fun sayHello(name: String): String
}