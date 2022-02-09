package com.longtech.rxjava.demo.example1

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.Keep
import androidx.core.os.bundleOf
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.launcher.ARouter
import com.longtech.arouter.applike.demo.ViewProvider
import com.longtech.arouter.applike.router.HelloService
import com.longtech.arouter.applike.router.MainActivity
import com.longtech.arouter.applike.router.arouter.ARouterManager
import com.longtech.arouter.applike.router.arouter.dispatch.DispatchProtocol
import com.longtech.arouter.applike.router.arouter.service.impl.ServiceARouter


class Example1 : ViewProvider.ViewHolder {
    val TAG: String = Example1::class.java.simpleName

    override fun title(): String {
        return TAG
    }

    override fun onCreate(context: Context, savedInstanceState: Bundle?) {
        init(context)
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
        var people = People()
        people.name = "xiezhihua"
        people.age = 11
        ARouterManager.getTestRouter().gotoMainActivity(bundleOf(), people)
    }

    /**
     * 统一路由协议
     */
    fun parseRouterProtocol(intent: Intent?): Boolean {
        return DispatchProtocol.navigation(intent)
    }

    @JvmField
    //@Autowired
    open var people: People? = null

    @JvmField
    //@Autowired(name = ServiceARouter.SERVICE_HOME_FRAGMENT)
    open var helloService: HelloService? = null

    fun init(context: Context) {
        ARouter.getInstance().inject(this);
        System.out.println(people?.toString())

        var helloService1 = ARouter.getInstance().navigation(HelloService::class.java)
        //Toast.makeText(context, helloService1.sayHello("xiezh"), Toast.LENGTH_LONG)

        var helloService2 = ARouter.getInstance().build(ServiceARouter.SERVICE_HOME_FRAGMENT).navigation() as HelloService
        //Toast.makeText(context, helloService2.sayHello("wangpei"), Toast.LENGTH_LONG)

        //Toast.makeText(context, helloService?.sayHello("longtech"), Toast.LENGTH_LONG).show()
    }

    @Keep
    class People() {
        open var name:String? = null
        open var age:Int? = null

        override fun toString(): String {
            return "name:" + name + "\n" +
                    "age:" + age + "\n"
        }
    }

    @Keep
    class Student() {
        open var name:String? = null
        open var age:Int? = null
    }
}