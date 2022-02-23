package com.longtech.eventbus.eventbus.example2

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import com.longtech.eventbus.applike.eventbus.ViewProvider
import com.longtech.eventbus.eventbus.example1.MessageEvent
import org.greenrobot.eventbus.EventBus

class Example2 : ViewProvider.ViewHolder {
    val TAG: String = Example2::class.java.simpleName
    
    override fun title(): String {
        return TAG
    }

    override fun onCreate(context: Context, savedInstanceState: Bundle?) {
        
    }

    override fun onStart() {
       
    }

    override fun onResume() {
       
    }

    override fun onPause() {
       
    }

    override fun onDestory() {
       
    }

    override fun onClick(view: View) {
        var event = MessageEvent()
        event.id = "120"
        EventBus.getDefault().post(event)
    }
}