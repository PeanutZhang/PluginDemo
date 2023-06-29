package com.jf.codecoverage

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.api.tasks.TaskExecutionException

class ClassInjectPlugin implements Plugin<Project>{
    @Override
    void apply(Project project) {
        Utils.prtln("classInjectPlugin","classInjectPlugin apply start execut =================")
        Utils.prtln("classinjectplugin ","start create aiconffig")

       AICConfig aicConfig = project.extensions.create("AICfg",AICConfig)//
        def mkdirTask = project.tasks.create("MkdirTask",MkdirTask.class)

        def copyTask = project.tasks.create("CopyClass",ClassCopyTask)
        copyTask.group = "andCoverage"
        copyTask.aicConfig = aicConfig

        def andExtension = project.extensions.findByType(BaseExtension)

        if(andExtension instanceof AppExtension){
            InjectFlagTranstrom transtrom = new InjectFlagTranstrom(project,aicConfig)
            andExtension.registerTransform(transtrom)

            // throw an exception in instant run mode
            andExtension.applicationVariants.all { variant ->
                def variantName = variant.name.capitalize()
                try {
                    def instantRunTask = project.tasks.getByName("transformClassesWithInstantRunFor${variantName}")
                    if (instantRunTask) {
                        throw new GradleException("不支持instant run")
                    }
                } catch (UnknownTaskException e) {

                }
            }
        }else {
            throw new TaskExecutionException(this, new Throwable("this not a application"))
        }

        Utils.prtln("jacocoPlugin #apply projcet.afterEvaluate")
        andExtension.applicationVariants.all { variant ->
            def variantName = variant.name.capitalize()
            if(mkdirTask){
                mkdirTask.aicConfig = aicConfig
                mkdirTask.mkEcFolder()
            }
            println "zyh----------->project: $project afterEvalute,  variantName $variantName"
            if (project.tasks.findByName('generateReport') == null) {
                println "zyh----------> tasks.findByTime('jfgenerateReport') is null "
                GenerateReportTask gpTask = project.tasks.create('generateReport', GenerateReportTask)
                gpTask.setGroup("andcoverage")
                gpTask.aicConfig = aicConfig

            }
        }
        project.afterEvaluate {
            project.tasks.each {
                if(it.name == "assembleDebug" || it.name == "assembleRelease"){
                    Utils.prtln("taskName: ${it.name}")
                    it.dependsOn(copyTask)
                }
            }
        }

    }






    private void applyMavenFeature(Project project) {
        project.afterEvaluate {
            // 1. Aiccofi extension
            // project.afterEvaluate() 生命周期监听，
            //因为扩展配置代码的执行时机晚于 Plugin#apply() 的执行时机，
            // 所以如果不使用 project.afterEvaluate()，
            // 则在插件内部将无法正确获取配置值
//            AICConfig rootConfig = AICConfig.getAIConfig(project)
            // 构建逻辑 ...
        }
    }

}
