package com.longtech.demo.demo.example6

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import com.longtech.demo.applike.demo.ViewProvider


class Example6 : ViewProvider.ViewHolder {
    val TAG: String = Example6::class.java.simpleName
    
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