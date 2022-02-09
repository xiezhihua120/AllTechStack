package com.longtech.arouter.applike.router.arouter.activity.impl

import android.os.Bundle
import androidx.annotation.Keep
import com.alibaba.android.arouter.launcher.ARouter
import com.longtech.arouter.applike.router.MainActivity
import com.longtech.arouter.applike.router.arouter.activity.ActivityPath
import com.longtech.rxjava.demo.example1.Example1

@Keep
object ActivityARouter: ActivityPath {
    const val GROUP_NAME = "/ActivityARouter/"

    override fun getGroupName(): String {
        return GROUP_NAME
    }

    const val TEST_MAIN_ACTIVITY: String = GROUP_NAME + "Main"
    const val TEST_TARGET_ACTIVITY: String = GROUP_NAME + "Target"

    /**
     * MainActivity首页
     */
    fun gotoMainActivity(bundle: Bundle = Bundle(), people: Example1.People? = null) {
        ARouter.getInstance().build(TEST_MAIN_ACTIVITY)
            .with(bundle)
            .withObject("people", people)
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