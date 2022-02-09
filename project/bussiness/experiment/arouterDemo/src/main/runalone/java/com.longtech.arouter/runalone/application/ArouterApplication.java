package com.longtech.arouter.runalone.application;

import android.app.Application;

import com.longtech.arouter.applike.router.arouter.ARouterManager;

/**
 * Created by mrzhang on 2017/6/20.
 */

public class ArouterApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ARouterManager.INSTANCE.init(this);
    }

}
