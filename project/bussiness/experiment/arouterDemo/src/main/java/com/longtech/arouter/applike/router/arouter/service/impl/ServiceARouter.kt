package com.longtech.arouter.applike.router.arouter.service.impl

import androidx.annotation.Keep
import com.longtech.arouter.applike.router.arouter.service.ServicePath

@Keep
object ServiceARouter: ServicePath {

    const val GROUP_NAME = "/ServiceARouter/"

    override fun getGroupName(): String {
        return GROUP_NAME
    }

    const val SERVICE_HOME_FRAGMENT: String = GROUP_NAME + "HomeFragment"
    const val SERVICE_DETAIL_FRAGMENT: String = GROUP_NAME + "DetailFragment"

}