package com.longtech.itransform

import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.gradle.api.Project

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.println

public class MyInjects {
    //初始化类池
    private final static ClassPool pool = ClassPool.getDefault();

    public static void inject(String path, Project project) {
        //将当前路径加入类池,不然找不到这个类
        pool.appendClassPath(path);
        //project.android.bootClasspath 加入android.jar，不然找不到android相关的所有类
        pool.appendClassPath(project.android.bootClasspath[0].toString());
        //引入android.os.Bundle包，因为onCreate方法参数有Bundle
        pool.importPackage("android.os.Bundle");

        File dir = new File(path);
        if (dir.isDirectory()) {
            //遍历文件夹
            dir.eachFileRecurse { File file ->
                String filePath = file.absolutePath
                println("filePath = " + filePath)
                if (file.getName().equals("MainActivity.class")) {

                    //获取MainActivity.class
                    try {
                        CtClass ctClass = pool.getCtClass("com.longtech.app.MainActivity");
                        if (ctClass != null)  {
                            println("ctClass = " + ctClass)
                            //解冻
                            if (ctClass.isFrozen())
                                ctClass.defrost()

                            //获取到OnCreate方法
                            CtMethod ctMethod = ctClass.getDeclaredMethod("onCreate")

                            println("方法名 = " + ctMethod)


                            String insetBeforeStr = """ android.widget.Toast.makeText(this,"WTF emmmmmmm.....我是被插入的Toast代码~!!",android.widget.Toast.LENGTH_LONG).show();
                                            """
                            //在方法开头插入代码

                            ctMethod.insertBefore(insetBeforeStr);
                            ctClass.writeFile(path)
                            ctClass.detach()//释放
                        }
                    } catch(Throwable e) {
                        e.printStackTrace()
                    }
                }
            }
        }

    }

}