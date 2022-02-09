package com.longtech.services

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Keep
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.facade.template.IProvider
import com.alibaba.android.arouter.launcher.ARouter
import com.longtech.services.dispatch.DispatchProtocol
import com.longtech.services.runalone.arouter.activity.impl.ActivityTest
import com.longtech.services.service.path.ServiceHome


@Route(path = ActivityTest.TEST_MAIN_ACTIVITY)
class ARouterDemoActivity : AppCompatActivity() {

    var textView: TextView? = null

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(999)

        textView = findViewById(999)
        textView?.text = "arouter"
        textView?.setOnClickListener {
            var people = People()
            people.name = "xiezhihua"
            people.age = 11

            ARouter.getInstance().build(ActivityTest.TEST_MAIN_ACTIVITY)
                .withObject("people", people)
                .navigation()
        }

        if (parseRouterProtocol(intent)) return
        init()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (parseRouterProtocol(intent)) return
    }

    /**
     * 统一路由协议
     */
    fun parseRouterProtocol(intent: Intent?): Boolean {
        return DispatchProtocol.navigation(intent)
    }

    @JvmField
    @Autowired
    open var people: People? = null

    @JvmField
    @Autowired(name = ServiceHome.SERVICE_HOME_FRAGMENT)
    open var helloService: HelloService? = null

    fun init() {
        ARouter.getInstance().inject(this);
        System.out.println(people?.toString())

        var helloService1 = ARouter.getInstance().navigation(HelloService::class.java)
        Toast.makeText(this, helloService1.sayHello("xiezh"), Toast.LENGTH_LONG)

        var helloService2 = ARouter.getInstance().build(ServiceHome.SERVICE_HOME_FRAGMENT).navigation() as HelloService
        Toast.makeText(this, helloService2.sayHello("wangpei"), Toast.LENGTH_LONG)

        Toast.makeText(this, helloService?.sayHello("longtech"), Toast.LENGTH_LONG).show()
    }

    @Keep
    class People() {
        var name:String? = null
        var age:Int? = null

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

    interface HelloService: IProvider {
        fun sayHello(name: String): String
    }

    @Route(path = ServiceHome.SERVICE_HOME_FRAGMENT, name = "测试服务")
    class HelloServiceImpl : HelloService {

        override fun sayHello(name: String): String {
            return "hello, $name"
        }

        override fun init(context: Context) {

        }
    }
}