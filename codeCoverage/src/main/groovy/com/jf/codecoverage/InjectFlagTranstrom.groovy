package com.jf.codecoverage
import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Status
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import groovy.io.FileType
import org.gradle.api.Project
import org.jacoco.core.diff.DiffAnalyzer

import static java.sql.DriverManager.println


class InjectFlagTranstrom extends Transform {

    Project mProject
    AICConfig aicConfig

    InjectFlagTranstrom(Project proect, AICConfig aicfg) {
        this.mProject = proect
        aicConfig = aicfg
    }

    @Override
    String getName() {
        return "zyhScan"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return true
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {

        Utils.prtln("injectFlagTransform transform start execut")

        def dirInputs = new HashSet<>()
        def jarInputs = new HashSet<>()
        println 'injectFlagTransform() stat execu ---------------------------- '
        if (!transformInvocation.isIncremental()) {
            transformInvocation.getOutputProvider().deleteAll()
        }

        transformInvocation.inputs.each {
            input ->
                input.directoryInputs.each { dirInput ->
                    dirInputs.add(dirInput)
                }
                input.jarInputs.each { jarInput ->
                    jarInputs.add(jarInput)
                }
        }
//        Utils.prtln("dirInputs.size ===> ${dirInputs.size()} jarInputs.size ==> ${jarInputs.size()}")
//        Utils.prtln("projectName: ${mProject.name}  project.projectDir  ${mProject.projectDir} p.buildDir ${mProject.buildDir} project.rootDir: ${mProject.rootDir}")

        if(dirInputs.isEmpty() && jarInputs.isEmpty()){
            Utils.prtln("dirInputs && jarInputs is empty")
            return
        }

        // 要处理的class 包名

        if(aicConfig.pluginEnable){
            //copy classes
            def classDir = "${mProject.projectDir}/classes"
            mProject.delete(classDir)
            mProject.mkdir(classDir)
            copy(transformInvocation, dirInputs, jarInputs, aicConfig.includes, null)
            // 获取差异方法列表，暂时固定
//            def currentDir = "${mProject.projectDir}/classes"
            def currentDir = "${mProject.rootDir.parentFile}${File.separator}${mProject.parent.name}classtmp${File.separator}cur"
//            def preDir = "${mProject.projectDir}/tmp/classes/pretag"
            def preDir = "${mProject.rootDir.parentFile}${File.separator}${mProject.parent.name}classtmp${File.separator}pre"
            try {
                DiffAnalyzer.getInstance().reset()
                DiffAnalyzer.readClasses(currentDir, DiffAnalyzer.CURRENT)
                DiffAnalyzer.readClasses(preDir, DiffAnalyzer.PRECOMMIT)
                DiffAnalyzer.getInstance().diff()
            } catch (Exception e) {
                Utils.prtln("read error:  ${e.getLocalizedMessage()}")

            }


            writerDiffMethodToFile()
        }

        inject(transformInvocation, dirInputs, jarInputs, aicConfig.includes)


    }

    //获得指定范围+类型的输出目标路径
    // outputprovider.getContentLocation

    def inject(TransformInvocation transformInvocation, def dirInputs, def jarInputs, List<String> includes) {
        Utils.prtln('start inject ======================================== ')
        ClassInjector injector = new ClassInjector(includes)
        if (!dirInputs.isEmpty()) {
            dirInputs.each { dirInput ->
                File dirOutput = transformInvocation.outputProvider.getContentLocation(dirInput.getName(),
                        dirInput.getContentTypes(), dirInput.getScopes(),
                        Format.DIRECTORY)
                FileUtils.mkdirs(dirOutput)

                if (transformInvocation.incremental) {
                    dirInput.changedFiles.each { entry ->
                        File fileInput = entry.getKey()
                        def fileInputAbsolutePath = fileInput.getAbsolutePath()
                        def dirInputFileAbsolutePath = dirInput.file.getAbsolutePath()
                        def dirOutputAbsolutePath = dirOutput.getAbsolutePath()
                        def fileOutTransPath = fileInputAbsolutePath.replace(
                                dirInputFileAbsolutePath, dirOutputAbsolutePath)
                        Utils.prtln("fileInputAbsolutePaht: ${fileInputAbsolutePath} \n diroutputAbsolutePaht: ${dirOutputAbsolutePath} \n fileOutTransPath: ${fileOutTransPath} ")

                        File fileOutputTransForm = new File(fileOutTransPath)
                        FileUtils.mkdirs(fileOutputTransForm.parentFile)
                        Status fileStatus = entry.getValue()
                        switch (fileStatus) {
                            case Status.ADDED:
                            case Status.CHANGED:
                                if (fileInput.isDirectory()) {
                                    return // continue.
                                }
                                def className = getClassName(fileInput)
                                Utils.prtln("incremental inject className $className")

                                if (aicConfig.pluginEnable && DiffAnalyzer.getInstance().containsClass(className)) {
                                    injector.doClass(fileInput, fileOutputTransForm)
                                } else {
                                    FileUtils.copyFile(fileInput, fileOutputTransForm)
                                }
                                break
                            case Status.REMOVED:
                                if (fileOutputTransForm.exists()) {
                                    if (fileOutputTransForm.isDirectory()) {
                                        fileOutputTransForm.deleteDir()
                                    } else {
                                        fileOutputTransForm.delete()
                                    }
                                    println("REMOVED output file Name:${fileOutputTransForm.name}")
                                }
                                break
                        }
                    }
                } else {
                    dirInput.file.traverse(type: FileType.FILES) { fileInput ->
//                        File fileOutputTransForm2 = new File(fileInput.getAbsolutePath().
//                        replace(dirInput.file.getAbsolutePath(), dirOutput.getAbsolutePath()))

                        def fileInputAbsolutePath = fileInput.getAbsolutePath()
                        def dirInputFileAbsPath = dirInput.file.getAbsolutePath()
                        def dirOutputAbsolutePath = dirOutput.getAbsolutePath()
                        def fileOutTransPath = fileInputAbsolutePath.replace(
                                dirInputFileAbsPath, dirOutputAbsolutePath)
                        Utils.prtln(" not incremental fileInputAbsolutePaht: ${fileInputAbsolutePath} \n diroutputAbsolutePaht: ${dirOutputAbsolutePath} \n fileOutTransPath: ${fileOutTransPath} ")
                        File fileOutputTransForm = new File(fileOutTransPath)
                        FileUtils.mkdirs(fileOutputTransForm.parentFile)
                        def className = getClassName(fileInput)
                        Utils.prtln(" incremental false inject className $className")
                        if (aicConfig.pluginEnable && DiffAnalyzer.getInstance().containsClass(className)) {
                            injector.doClass(fileInput, fileOutputTransForm)
                        } else {
                            FileUtils.copyFile(fileInput, fileOutputTransForm)
                        }
                    }
                }
            }
        }

        if (!jarInputs.isEmpty()) {
            jarInputs.each { jarInput ->

                File jarInputFile = jarInput.file
                File jarOutputFile = transformInvocation.outputProvider.getContentLocation(
                        jarInput.getName(), jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR
                )


                FileUtils.mkdirs(jarOutputFile.parentFile)

                switch (jarInput.status) {
                    case Status.NOTCHANGED:
                        if (transformInvocation.incremental) {
                            break
                        }
                    case Status.ADDED:
                    case Status.CHANGED:
                        if (aicConfig.pluginEnable) {
                            injector.doJar(jarInputFile, jarOutputFile)
                        } else {
                            FileUtils.copyFile(jarInputFile, jarOutputFile)
                        }
                        break
                    case Status.REMOVED:
                        if (jarOutputFile.exists()) {
                            jarOutputFile.delete()
                        }
                        break
                }
            }
        }
    }


    def copy(TransformInvocation transformInvocation, def dirInputs, def jarInputs, List<String> includes, def savePath) {
        def classDir = "${mProject.projectDir}/classes"
        if (savePath) {
            classDir = savePath
        }
        println "start copy classess======================== classDir: $classDir "
        ClassCopier copier = new ClassCopier(classDir, includes)
        if (!transformInvocation.incremental) {
            FileUtils.deletePath(new File(classDir))
        }
        if (!dirInputs.isEmpty()) {
            dirInputs.each { dirInput ->
                if (transformInvocation.incremental) {
                    dirInput.changedFiles.each { entry ->
                        File fileInput = entry.getKey()
                        File fileOutputJacoco = new File(fileInput.getAbsolutePath().replace(dirInput.file.getAbsolutePath(), classDir))
                        Status fileStatus = entry.getValue()

                        switch (fileStatus) {
                            case Status.ADDED:
                            case Status.CHANGED:
                                if (fileInput.isDirectory()) {
                                    return // continue.
                                }
                                copier.doClass(fileInput, fileOutputJacoco)
                                break
                            case Status.REMOVED:
                                if (fileOutputJacoco.exists()) {
                                    if (fileOutputJacoco.isDirectory()) {
                                        fileOutputJacoco.deleteDir()
                                    } else {
                                        fileOutputJacoco.delete()
                                    }
                                    println("REMOVED output file Name:${fileOutputJacoco.name}")
                                }
                                break
                        }
                    }
                } else {
                    dirInput.file.traverse(type: FileType.FILES) { fileInput ->
                        File fileOutputJacoco = new File(fileInput.getAbsolutePath().replace(dirInput.file.getAbsolutePath(), classDir))
                        copier.doClass(fileInput, fileOutputJacoco)
                    }
                }
            }
        }

        if (!jarInputs.isEmpty()) {
            jarInputs.each { jarInput ->
                File jarInputFile = jarInput.file
                copier.doJar(jarInputFile, null)
            }
        }
    }


    def getClassName(File f) {
        return ClassProcessor.filePath2ClassName(f).replaceAll(".class", "")
    }

    def writerDiffMethodToFile() {
        String path = "${mProject.projectDir}${File.separator}classes${File.separator}diff${File.separator}diffMethod.txt"
        println("writerDiffMethodToFile size=" + DiffAnalyzer.getInstance().getDiffList().size() + " >" + path);

        try {
            FileUtils.writeToFile(new File(path), DiffAnalyzer.getInstance().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
