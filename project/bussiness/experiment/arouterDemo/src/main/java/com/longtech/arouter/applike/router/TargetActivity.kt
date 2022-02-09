package com.longtech.arouter.applike.router

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.longtech.arouter.R
import com.longtech.arouter.applike.router.arouter.ARouterManager
import com.longtech.arouter.applike.router.arouter.activity.impl.ActivityARouter

@Route(path = ActivityARouter.TEST_TARGET_ACTIVITY)
class TargetActivity : AppCompatActivity() {

    var textView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.scrollView)
        textView?.text = "target"
        textView?.setOnClickListener {
            ARouterManager.getTestRouter().gotoMainActivity()
        }
    }
}