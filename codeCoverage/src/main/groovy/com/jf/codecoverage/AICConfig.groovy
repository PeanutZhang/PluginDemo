package com.jf.codecoverage


import org.gradle.api.Project

// custom extension
class AICConfig{
    def name
    AICConfig() {
        Utils.prtln("AICConfig constructor executed------")
    }

    List<String> sourceDirectories
    //exec文件路径，支持多个ec文件，自动合并
    String execDir
    //class目录
    List<String> classDirectories

    //生成报告的目录
    String reportDirectory

    List<String> includes

    String preCommitId
    boolean  pluginEnable
    List<String> includeModule

    String checkCommitShell

    static  AICConfig getAIConfig(Project project){

        AICConfig extension = project.getExtensions().findByType(AICConfig.class)
        if(extension == null){
            extension = new AICConfig()
        }
        return extension
    }

    String getGitBashPath() {
        if (gitBashPath == null || gitBashPath.isEmpty()) {
            Process process = 'where git'.execute()
            String path = process.inputStream.text
            process.closeStreams()
            String[] paths = path.split('\n')
            String temp = ''
            paths.each {
                File file = new File(it)
                File gitBash = new File(file.getParentFile().getParent() + File.separator + 'git-bash.exe')
                println("GitBashPath:$gitBash exist:${gitBash.exists()}")
                if (gitBash.exists()) {
                    temp = gitBash.absolutePath
                    return temp
                }
            }
            return temp
        }
        return gitBashPath
    }

}