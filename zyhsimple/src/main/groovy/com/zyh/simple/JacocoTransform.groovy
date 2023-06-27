package com.zyh.simple

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import com.zyh.simple.extension.JacocoExtension
import com.zyh.simple.task.BranchDiffTask
import groovy.io.FileType
import org.codehaus.groovy.runtime.IOGroovyMethods
import org.gradle.api.Project

class JacocoTransform extends Transform {
    Project project

    JacocoExtension jacocoExtension

    JacocoTransform(Project project, JacocoExtension jacocoExtension) {
        this.project = project
        this.jacocoExtension = jacocoExtension
    }

    @Override
    String getName() {
        return "jacoco"
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
        def dirInputs = new HashSet<>()
        def jarInputs = new HashSet<>()
       println "$Utils.ZTAG jacocoTransform() stat execu ---------------------------- "
        if (!transformInvocation.isIncremental()) {
            transformInvocation.getOutputProvider().deleteAll()
        }
        transformInvocation.inputs.each { input ->
            input.directoryInputs.each { dirInput ->
                dirInputs.add(dirInput)
            }
            input.jarInputs.each { jarInput ->
                jarInputs.add(jarInput)
            }
        }
        println "==========fff=====flag====================="

        if (!dirInputs.isEmpty() || !jarInputs.isEmpty()) {
            if (jacocoExtension.jacocoEnable) {
                //copy class到 app/classes
                copy(transformInvocation, dirInputs, jarInputs, jacocoExtension.includes)
                //提交classes 到git
//                gitPush(jacocoExtension.gitPushShell, "jacoco auto commit")
                //获取差异方法集
//                BranchDiffTask branchDiffTask = project.tasks.findByName('generateReport')
//                branchDiffTask.pullDiffClasses()
            }
            //对diff方法插入探针
//            inject(transformInvocation, dirInputs, jarInputs, jacocoExtension.includes)

        }




    }

    def copy(TransformInvocation transformInvocation, def dirInputs, def jarInputs, List<String> includes) {
        def classDir = "${project.projectDir}/classes"
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


}