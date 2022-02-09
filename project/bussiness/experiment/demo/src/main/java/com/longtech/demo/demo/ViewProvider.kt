package com.longtech.demo.applike.demo

import android.content.Context
import android.os.Bundle
import android.view.View
import com.longtech.demo.demo.example1.Example1
import com.longtech.demo.demo.example2.Example2
import com.longtech.demo.demo.example3.Example3
import com.longtech.demo.demo.example4.Example4
import com.longtech.demo.demo.example5.Example5
import com.longtech.demo.demo.example6.Example6
import com.longtech.demo.demo.example7.Example7
import com.longtech.demo.demo.example8.Example8
import com.longtech.demo.demo.example9.Example9

class ViewProvider {

    interface ViewHolder {
        fun title(): String
        fun onCreate(context: Context, savedInstanceState: Bundle?)
        fun onStart()
        fun onResume()
        fun onPause()
        fun onDestory()
        fun onClick(view: View)
    }

    companion object {
        private var holders = mutableListOf<ViewHolder>()

        init {
            holders.add(Example1())
            holders.add(Example2())
            holders.add(Example3())
            holders.add(Example4())
            holders.add(Example5())
            holders.add(Example6())
            holders.add(Example7())
            holders.add(Example8())
            holders.add(Example9())
        }

        fun getHolders(): MutableList<ViewHolder> {
            return holders
        }
    }
}