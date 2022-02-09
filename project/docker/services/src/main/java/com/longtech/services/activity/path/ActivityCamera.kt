package com.longtech.services.runalone.arouter.activity.impl

import android.os.Bundle
import androidx.annotation.Keep
import com.alibaba.android.arouter.launcher.ARouter
import com.longtech.services.runalone.arouter.service.ActivityPath

@Keep
object ActivityCamera: ActivityPath {
    const val GROUP_NAME = "/ActivityCamera/"

    override fun getGroupName(): String {
        return GROUP_NAME
    }

    const val CAMERA_HOME_ACTIVITY: String = GROUP_NAME + "Home"
    const val CAMERA_DETAIL_ACTIVITY: String = GROUP_NAME + "Detail"
    const val CAMERA_LIST_ACTIVITY: String = GROUP_NAME + "List"

    /**
     * 相机首页
     */
    fun gotoHomeActivity(bundle: Bundle = Bundle()) {
        ARouter.getInstance().build(CAMERA_HOME_ACTIVITY)
            .with(bundle)
            .navigation()
    }

    /**
     * 相机详情
     */
    fun gotoDetailActivity(bundle: Bundle = Bundle()) {
        ARouter.getInstance().build(CAMERA_DETAIL_ACTIVITY)
            .with(bundle)
            .navigation()
    }

    /**
     * 相机列表
     */
    fun gotoListActivity(bundle: Bundle = Bundle()) {
        ARouter.getInstance().build(CAMERA_LIST_ACTIVITY)
            .with(bundle)
            .navigation()
    }

}