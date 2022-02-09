package com.longtech.glide.demo.runalone.application;

import android.app.Application;

import com.longtech.glide.demo.GlideImageLoader;
import com.longtech.glide.demo.R;
import com.luojilab.component.componentlib.router.Router;

/**
 * Created by mrzhang on 2017/6/20.
 */

public class GlideApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //如果isRegisterCompoAuto为false，则需要通过反射加载组件
        Router.registerComponent("com.luojilab.share.applike.ShareApplike");
        Router.registerComponent("com.luojilab.share.kotlin.applike.KotlinApplike");

        GlideImageLoader.getInstance().initDefultImg(
                R.drawable.common_defult_image,
                R.drawable.common_defult_image,
                R.drawable.common_defult_circle_image,
                R.drawable.common_defult_circle_image
        );
    }

}
