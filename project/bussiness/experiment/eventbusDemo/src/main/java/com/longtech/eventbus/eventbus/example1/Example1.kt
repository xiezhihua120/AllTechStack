package com.longtech.eventbus.eventbus.example1

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.longtech.eventbus.applike.eventbus.ViewProvider
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class Example1 : ViewProvider.ViewHolder {
    val TAG: String = Example1::class.java.simpleName
    lateinit var context: Context

    override fun title(): String {
        return TAG
    }

    override fun onCreate(context: Context, savedInstanceState: Bundle?) {
        this.context = context
        EventBus.getDefault().register(this)
    }

    override fun onStart() {

    }

    override fun onResume() {
       
    }

    override fun onPause() {
       
    }

    override fun onDestory() {
        EventBus.getDefault().unregister(this)
    }

    override fun onClick(view: View) {
        var event = MessageEvent()
        event.id = "110"
        EventBus.getDefault().post(event)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onHandleEvent(event: MessageEvent) {
        Toast.makeText(this.context, event.id, Toast.LENGTH_LONG).show()
    }
}