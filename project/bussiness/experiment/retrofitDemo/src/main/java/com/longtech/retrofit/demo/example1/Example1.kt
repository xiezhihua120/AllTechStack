package com.longtech.retrofit.demo.example1

import android.content.Context
import android.os.Bundle
import android.view.View
import com.longtech.retrofit.demo.ViewProvider


class Example1 : ViewProvider.ViewHolder {
    val TAG: String = Example1::class.java.simpleName

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