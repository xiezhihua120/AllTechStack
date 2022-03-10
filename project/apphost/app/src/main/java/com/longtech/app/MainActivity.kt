package com.longtech.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Debug
import android.util.Log
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.longtech.app.initialize.Base

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.login).setOnClickListener {
            onClick()
        }
    }

    fun onClick() {
        Log.d("CheckLoginAspect", "onClick")

        //ARouter.getInstance().build("/okhttp/main").navigation(this)
        ARouter.getInstance().build("/retrofit2/main").navigation(this)

        var base = Base("xiezh", 11)
        base.fullName = "1"

    }
}