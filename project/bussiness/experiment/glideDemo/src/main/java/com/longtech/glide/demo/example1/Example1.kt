package com.longtech.glide.glide.example1

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import com.longtech.glide.applike.glide.ViewProvider
import com.longtech.glide.demo.example1.RequestOptions

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

        var options2 = RequestOptions.priorityOf(1)
        Log.e("RequestOptions", options2.hashCode().toString())
        var options3 = options2.override(100, 100)
        Log.e("RequestOptions", options3.hashCode().toString())
        var options4 = options3.disableAnimation()
        Log.e("RequestOptions", options4.hashCode().toString())
        var options5 = options4.priority(1)
        Log.e("RequestOptions", options5.hashCode().toString())
        var options6 = options5.override(100, 100)
        Log.e("RequestOptions", options6.hashCode().toString())
        var options7 = options6.disableAnimation()
        Log.e("RequestOptions", options7.hashCode().toString())
        var options8 = options7.priority(1)
        Log.e("RequestOptions", options8.hashCode().toString())


        RequestOptions.main(null)
    }

}