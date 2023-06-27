package com.zyh.hhh

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.Project

/**
 * 定义一个Transform
 */
class ToastfTransform extends Transform {

    private Project mProject

    // 构造函数，我们将Project保存下来备用
    ToastfTransform(Project project) {
        this.mProject = project
        println '-----------toastTransform constrouctor'
    }

    // 设置我们自定义的Transform对应的Task名称
    // 类似：transformClassesWithPreDexForXXX
    // 这里应该是：transformClassesWithInjectTransformForxxx
    @Override
    String getName() {
        return 'InjectTransform'
    }

    // 指定输入的类型，通过这里的设定，可以指定我们要处理的文件类型
    //  这样确保其他类型的文件不会传入
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    // 指定Transform的作用范围
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    // 当前Transform是否支持增量编译
    @Override
    boolean isIncremental() {
        return false
    }

    // 核心方法
    // inputs是传过来的输入流，有两种格式：jar，  目录结构和class文件，
    // 是输入文件的抽象，
     //
    // outputProvider 获取输出目录，将修改的文件复制到输出目录，必须执行
    //每个 Transform 其实都是一个 Gradle 的 Task ， Android 编译器中的 TaskManager 会将每个 Transform 串联起来。
    // 第一个 Transform 接收来自 javac 编译的结果，以及拉取到本地的第三方依赖和 resource 资源。
    // 这些编译的中间产物在 Transform 链上流动，每个 Transform 节点都可以对 class 进行处理再传递到下一个 Transform
    //
    @Override
    void transform(Context context, Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider,
                   boolean isIncremental) throws IOException, TransformException, InterruptedException {
        println '--------buildsrc------------transform 开始-------------------'

        // Transform的inputs有两种类型，一种是目录，一种是jar包，要分开遍历
        inputs.each {
            TransformInput input ->
                // 遍历文件夹
                //文件夹里面包含的是我们手写的类以及R.class、BuildConfig.class以及R$XXX.class等
                input.directoryInputs.each {
                    DirectoryInput directoryInput ->
                        // 注入代码
                        com.zyh.simple.SimpleInjectByJavassit.injectToast(directoryInput.file.absolutePath, mProject)

                        // 获取输出目录
                        def dest = outputProvider.getContentLocation(directoryInput.name,
                                directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
// /home/zyh/XM/as_space/auto_test/PluginDemo/app/build/intermediates/transforms/InjectTransform/debug/33
                        println("---------------------->  directory output dest: $dest.absolutePath")
                        // 将input的目录复制到output指定目录
                        FileUtils.copyDirectory(directoryInput.file, dest)
                }

                //对类型为jar文件的input进行遍历
                input.jarInputs.each {
                        //jar文件一般是第三方依赖库jar文件
                    JarInput jarInput ->
                        // 重命名输出文件（同目录copyFile会冲突）
                        def jarName = jarInput.name
                        println("jar: $jarInput.file.absolutePath")
                        def md5Name = DigestUtils.md5Hex(jarInput.file.absolutePath)
                        if (jarName.endsWith('.jar')) {
                            jarName = jarName.substring(0, jarName.length() - 4)
                        }
                        def dest = outputProvider.getContentLocation(jarName + md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)

                        println("jar output dest: $dest.absolutePath")
                        FileUtils.copyFile(jarInput.file, dest)
                }
        }

        println '---------------------transform 结束-------------------'
    }
}
