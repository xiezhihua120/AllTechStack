package com.longtech.services.service.path

import androidx.annotation.Keep
import com.longtech.services.service.ServicePath

@Keep
object ServiceHome: ServicePath {

    const val GROUP_NAME = "/ServiceHome/"

    override fun getGroupName(): String {
        return GROUP_NAME
    }

    const val SERVICE_HOME_FRAGMENT: String = GROUP_NAME + "HomeFragment"
    const val SERVICE_DETAIL_FRAGMENT: String = GROUP_NAME + "DetailFragment"

}