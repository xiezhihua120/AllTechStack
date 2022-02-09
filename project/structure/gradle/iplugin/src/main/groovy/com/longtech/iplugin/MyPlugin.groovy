package com.longtech.iplugin

import org.gradle.api.Plugin
import org.gradle.api.Project

public class MyPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        println("========================");
        println("hello gradle plugin!");
        println("========================");
    }
}