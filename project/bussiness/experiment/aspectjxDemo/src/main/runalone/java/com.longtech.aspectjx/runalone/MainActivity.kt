package com.longtech.aspectjx.runalone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.longtech.aspectjx.R

class MainActivity : AppCompatActivity() {


    var textView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.name)
        textView?.text = "aspectjx"
    }

}