package com.longtech.iaspectj

import org.aspectj.bridge.IMessage
import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main
import org.gradle.api.Plugin
import org.gradle.api.Project
/**
 * @author：xinyu.zhou
 * @version: 2018/9/19
 * @ClassName:
 * @Description: ${todo}(这里用一句话描述这个类的作用)
 */
public class AspectjPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.dependencies {
            implementation 'org.aspectj:aspectjrt:1.9.6', {
                exclude group: 'com.android.support'
            }
        }
        final def log = project.logger
        log.error "========================";
        log.error "Aspectj切片开始编织Class!";
        log.error "========================";

        if (project.android.hasProperty('applicationVariants')) {
            project.android.applicationVariants.all { variant ->
                def javaCompile = variant.javaCompile
                javaCompile.doLast {
                    String[] args = ["-showWeaveInfo",
                                     "-1.8",
                                     "-inpath", javaCompile.destinationDir.toString(),
                                     "-aspectpath", javaCompile.classpath.asPath,
                                     "-d", javaCompile.destinationDir.toString(),
                                     "-classpath", javaCompile.classpath.asPath,
                                     "-bootclasspath", project.android.bootClasspath.join(File.pathSeparator)]
                    log.debug "ajc args: " + Arrays.toString(args)

                    MessageHandler handler = new MessageHandler(true);
                    new Main().run(args, handler);
                    for (IMessage message : handler.getMessages(null, true)) {
                        switch (message.getKind()) {
                            case IMessage.ABORT:
                            case IMessage.ERROR:
                            case IMessage.FAIL:
                                log.error message.message, message.thrown
                                break;
                            case IMessage.WARNING:
                                log.warn message.message, message.thrown
                                break;
                            case IMessage.INFO:
                                log.info message.message, message.thrown
                                break;
                            case IMessage.DEBUG:
                                log.debug message.message, message.thrown
                                break;
                        }
                    }
                }
            }
        }
    }
}