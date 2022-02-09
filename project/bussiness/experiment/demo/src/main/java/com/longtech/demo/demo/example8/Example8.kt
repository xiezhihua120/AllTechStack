package com.longtech.demo.demo.example8

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import com.longtech.demo.applike.demo.ViewProvider


class Example8 : ViewProvider.ViewHolder {
    val TAG: String = Example8::class.java.simpleName
    
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