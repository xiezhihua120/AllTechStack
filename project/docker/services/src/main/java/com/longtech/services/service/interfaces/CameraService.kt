package com.longtech.services.service.interfaces

import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.template.IProvider

interface CameraService: IProvider {
    fun getHomeFragment(name: String): Fragment
}