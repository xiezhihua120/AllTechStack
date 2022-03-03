package com.longtech.app.initialize.blockcanary;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.github.moduth.blockcanary.BlockCanaryContext;
import com.longtech.app.BuildConfig;

/**
 * BlockCanary初始化
 * BlockCanary.install(this, new AppBlockCanaryContext()).start();
 */
public class AppBlockCanaryContext extends BlockCanaryContext {
    private static final String TAG = "AppBlockCanaryContext";

    @Override
    public String provideQualifier() {
        String qualifier = "";
        try {
            PackageInfo info = provideContext().getPackageManager().getPackageInfo(provideContext().getPackageName(), 0);
            qualifier += info.versionCode + "_" + info.versionName + "_YYB";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return qualifier;
    }

    @Override
    public int provideBlockThreshold() {
        return 500;
    }

    @Override
    public boolean displayNotification() {
        return BuildConfig.DEBUG;
    }

    @Override
    public boolean stopWhenDebugging() {
        return true;
    }
}