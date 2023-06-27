package com.zyh.hhh;

import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import javassist.bytecode.AnnotationsAttribute
import org.gradle.api.Project;

class SimpleInjectByJavassit {

    private static final ClassPool sClassPool = ClassPool.getDefault()

    /**
     * 插入一段Toast代码
     * @param path
     * @param project
     */
    static void injectToast(String path, Project project) {
        // 加入当前路径
        sClassPool.appendClassPath(path)
        // project.android.bootClasspath 加入android.jar，不然找不到android相关的所有类
        sClassPool.appendClassPath(project.android.bootClasspath[0].toString())
        // 引入android.os.Bundle包，因为onCreate方法参数有Bundle
        sClassPool.importPackage('android.os.Bundle')

        File dir = new File(path)
        if (dir.isDirectory()) {
            // 遍历文件夹
            dir.eachFileRecurse { File file ->
                String filePath = file.absolutePath
                println("----------->> filePath: $filePath")

                if (file.name.endsWith('Activity.class')) {
                    // 获取Class
                    // 这里的MainActivity就在app模块里
                    CtClass ctClass = sClassPool.makeClass(new FileInputStream(file))
                    println("ctClass: $ctClass")
                    // 解冻
                    if (ctClass.isFrozen()) {
                        ctClass.defrost()
                    }

                    // 获取Method
                    CtMethod ctMethod = ctClass.getDeclaredMethod('onCreate')
                    println("------>> ctMethod: $ctMethod")

                    String toastStr = """ android.widget.Toast.makeText(this,"the insert toast wa gagagggg",android.widget.Toast.LENGTH_LONG).show();  
                                      """
                    AnnotationsAttribute attribute = (AnnotationsAttribute) ctMethod.getMethodInfo().getAttribute(AnnotationsAttribute.invisibleTag);
                    if (attribute != null && attribute.getAnnotation("com.zyh.plugindemo.Hello") != null) {
                        println("----->  Insert toast in " + ctClass.getSimpleName())
                        ctMethod.insertAfter(toastStr)
                    }
                    // 方法尾插入
//                    ctMethod.insertAfter(toastStr)
                    ctClass.writeFile(path)
                    ctClass.detach() //释放
                }
            }
        }
    }

}
