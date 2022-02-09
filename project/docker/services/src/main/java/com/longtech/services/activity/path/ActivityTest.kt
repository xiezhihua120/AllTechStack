package com.longtech.services.runalone.arouter.activity.impl

import android.os.Bundle
import androidx.annotation.Keep
import com.alibaba.android.arouter.launcher.ARouter
import com.longtech.services.runalone.arouter.service.ActivityPath

@Keep
object ActivityTest: ActivityPath {
    const val GROUP_NAME = "/ActivityTest/"

    override fun getGroupName(): String {
        return GROUP_NAME
    }

    const val TEST_MAIN_ACTIVITY: String = GROUP_NAME + "Main"
    const val TEST_TARGET_ACTIVITY: String = GROUP_NAME + "Target"

    /**
     * MainActivity首页
     */
    fun gotoMainActivity(bundle: Bundle = Bundle()) {
        ARouter.getInstance().build(TEST_MAIN_ACTIVITY)
            .with(bundle)
            .navigation()
    }

    /**
     * MainActivity首页
     */
    fun gotoTargetActivity(bundle: Bundle = Bundle()) {
        ARouter.getInstance().build(TEST_TARGET_ACTIVITY)
            .with(bundle)
            .navigation()
    }
}