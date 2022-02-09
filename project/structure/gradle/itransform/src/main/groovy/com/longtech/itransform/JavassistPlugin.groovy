package com.longtech.itransform

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

public class JavassistPlugin implements Plugin<Project> {

    void apply(Project project) {
        System.out.println("------------------开始----------------------");
        System.out.println("这是我们的自定义插件!");
        //AppExtension就是build.gradle中android{...}这一块
        def android = project.extensions.getByType(AppExtension)

        //注册一个Transform
        def classTransform = new JavassistTransform(project);
        android.registerTransform(classTransform);

        System.out.println("------------------结束了吗----------------------");
    }
}