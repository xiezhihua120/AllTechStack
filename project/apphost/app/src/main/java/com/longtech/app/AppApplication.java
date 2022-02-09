package com.longtech.app;
import android.app.Application;

import com.longtech.services.ARouterManager;

public class AppApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ARouterManager.INSTANCE.init(this);
    }

}
