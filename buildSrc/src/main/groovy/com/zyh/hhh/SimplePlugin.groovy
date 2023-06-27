package com.zyh.hhh


import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class SimplePlugin implements Plugin<Project> {
   def  TAG = '---simplePlugin------->'
    @Override
    void apply(Project project) {
//        androidEx.registerTransform(new ToastfTransform(project))
       println "simple plugin apply ()+++++++++++++++++++++++++++"
       def t = project.tasks.create("doSthUwant",SimpleTask.class)
       t.group = "simpledemo"

//       t.doCusSth()
        // ./gradlew xxx -PtestValue=xxx
        def v2 = project.getProperties().get("testValue")
        def useZyhSimple = project.getProperties().get("useZyhsimple")
        Utils.prtln("testValue>>   v2: $v2")
        Utils.prtln("useZysimple>> : $useZyhSimple")
      println "wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww"
//
//        project.afterEvaluate {//配置完成之后执行
//            println "$Utils.ZTAG jacocoPlugin #apply projcet.afterEvaluate"
//            android.applicationVariants.all { variant ->
//                def variantName = variant.name.capitalize()
//                println "zyh-----------> project: $project afterEvalute,  variantName $variantName"
//                if (project.tasks.findByName('generateReport') == null) {
//                    println "zyh----------> tasks.findByTime('generateReport') is null "
//                    BranchDiffTask branchDiffTask = project.tasks.create('generateReport', BranchDiffTask)
//                    branchDiffTask.setGroup("simpleJacoco")
//                    branchDiffTask.jacocoExtension = jacocoExtension
//                }
//            }
//        }


    }
}