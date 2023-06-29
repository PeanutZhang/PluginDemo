package com.jf.codecoverage


import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class MkdirTask extends DefaultTask {
    @Input
    AICConfig aicConfig
    @TaskAction
    void mkEcFolder() {
        Utils.prtln("mkdirTask fff---------- ")
        def  ecDir
        if(aicConfig){
            ecDir = aicConfig.execDir
        }else {
             ecDir = "${project.buildDir}${File.separator}outputs${File.separator}coverage"
        }
        File f = new File(ecDir)
        if(!f.exists()){
            project.mkdir(ecDir)
        }
    }

}