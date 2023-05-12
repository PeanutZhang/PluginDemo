package com.zyh.simple

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project


class SimplePlugin implements Plugin<Project> {
   def  TAG = '---simplePlugin------->'
    @Override
    void apply(Project project) {
        println "------===================---->  simplePlugin apply $project"
        def androidEx = project.extensions.getByType(BaseExtension.class)
        println 'register transform toastfft'
        androidEx.registerTransform(new ToastfTransform(project))
        // 这里只是随便定义一个Task而已，和Transform无关
        project.tasks.create('SimpleTask') {
          doFirst {
                println 'fuck the simpleTask  do first'
            }

           doLast {
                println('fuck the simpleTask  dolast  reg toasttraom' )
            }
        }

    }
}