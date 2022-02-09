package com.longtech.camera.runalone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.longtech.bindannotation.BindView
import com.longtech.bindannotation.BindViewTools
import com.longtech.camera.Camera
import com.longtech.camera.R

class MainActivity : AppCompatActivity() {

    @BindView(R.id.name)
    var textView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        BindViewTools.bind(this)
        Camera()
        textView?.text = "camera"
    }

}