package com.longtech.eventbus.eventbus.example5

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import com.longtech.eventbus.applike.eventbus.ViewProvider


class Example5 : ViewProvider.ViewHolder {
    val TAG: String = Example5::class.java.simpleName
    
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