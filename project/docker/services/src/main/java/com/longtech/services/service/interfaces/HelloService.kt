package com.longtech.services.service.interfaces

import com.alibaba.android.arouter.facade.template.IProvider

interface HelloService: IProvider {
    fun sayHello(name: String): String
}