package com.longtech.retrofit.runalone.application;

import android.app.Application;

import com.longtech.services.ARouterManager;
import com.luojilab.component.componentlib.router.Router;

/**
 * Created by mrzhang on 2017/6/20.
 */

public class OKHttpApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //如果isRegisterCompoAuto为false，则需要通过反射加载组件
        //Router.registerComponent("com.luojilab.share.applike.ShareApplike");
        //Router.registerComponent("com.luojilab.share.kotlin.applike.KotlinApplike");

        ARouterManager.INSTANCE.init(this);
    }

}
