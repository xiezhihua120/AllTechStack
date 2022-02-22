package com.longtech.eventbus.eventbus.example3

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import com.longtech.eventbus.applike.eventbus.ViewProvider
import java.lang.Exception

class Example3 : ViewProvider.ViewHolder {
    val TAG: String = Example3::class.java.simpleName
    
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

    }
}