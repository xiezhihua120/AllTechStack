package com.longtech.rxjava.demo.example5

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import com.longtech.rxjava.demo.ViewProvider
import io.reactivex.rxjava3.core.Observable

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

        Observable.just(1, 2, 3, 4, 5)
            .scan { integer1, integer2 ->
                Log.d(TAG, "                          ")
                Log.d(TAG, "********************apply ")
                Log.d(TAG, "====================integer " + integer1)
                Log.d(TAG, "====================integer2 " + integer2)
                integer1 + integer2
            }
            .subscribe { integer ->
                Log.d(TAG, "====================accept " + integer)
            }
    }
}