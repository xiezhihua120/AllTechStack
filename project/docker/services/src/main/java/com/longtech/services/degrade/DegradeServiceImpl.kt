package com.longtech.services.degrade

import android.content.Context
import android.widget.Toast
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.facade.service.DegradeService


// 实现DegradeService接口，并加上一个Path内容任意的注解即可
@Route(path = "/global/degrade")
class DegradeServiceImpl : DegradeService {

    override fun onLost(context: Context?, postcard: Postcard?) {
        // do something.
        if (context != null) {
            Toast.makeText(context, "无法找到目标页面", Toast.LENGTH_LONG).show()
        }
    }

    override fun init(context: Context?) {

    }

}