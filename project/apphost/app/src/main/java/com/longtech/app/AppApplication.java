package com.longtech.app;
import android.app.Application;

import com.github.moduth.blockcanary.BlockCanary;
import com.longtech.app.initialize.blockcanary.AppBlockCanaryContext;
import com.longtech.services.ARouterManager;

public class AppApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ARouterManager.INSTANCE.init(this);
    }

}
