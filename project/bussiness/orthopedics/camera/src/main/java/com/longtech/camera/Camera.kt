package com.longtech.camera

import com.longtech.customui.export.CustomUI
import com.longtech.interfaces.Interfaces

class Camera {

    fun test() {
        CustomUI()
        Interfaces.imageLoader.loadImage("www.baidu.com")
        Interfaces.downloader.start("www.baidu.com")
        Interfaces.passport.login("www.baidu.com", "123456")
    }

}