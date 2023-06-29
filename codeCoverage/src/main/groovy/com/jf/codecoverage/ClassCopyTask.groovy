package com.jf.codecoverage

import com.sun.xml.bind.v2.TODO
import org.codehaus.groovy.runtime.IOGroovyMethods
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException

class ClassCopyTask extends DefaultTask {

    @Internal
    AICConfig aicConfig
    @Optional
    @OutputDirectory
    def preDir = project.mkdir("${project.rootDir.parentFile}${File.separator}${project.parent.name}classtmp")

    @Optional
    @Input
    def inputFlag

    @TaskAction
    def doCopy() {

        println('class copy task start copy classes ----------')

        inputFlag = "flag"

//        def preDir = "${project.rootDir.parentFile}${File.separator}${project.parent.name}classtmp"
//        preDir = project.mkdir(preDir)
        def preJavaComDir = "${preDir.absolutePath}${File.separator}pre${File.separator}java"
        def preKotlinComDir = "${preDir.absolutePath}${File.separator}pre${File.separator}kotlin"
        project.delete(preJavaComDir)
        project.mkdir(preJavaComDir)
        project.delete(preKotlinComDir)
        project.mkdir(preKotlinComDir)


        def curJavaComDir = "${project.rootDir.parentFile}${File.separator}${project.parent.name}classtmp${File.separator}cur${File.separator}java"
        def curKotlinComDir = "${project.rootDir.parentFile}${File.separator}${project.parent.name}classtmp${File.separator}cur${File.separator}kotlin"
        project.delete(curJavaComDir)
        project.mkdir(curJavaComDir)
        project.delete(curKotlinComDir)
        project.mkdir(curKotlinComDir)

        boolean isWindows = Utils.isWindows()
        def curBranchName = "git name-rev --name-only HEAD".execute().text.replaceAll("\n", "")
        Utils.prtln("branchName $curBranchName")

        project.gradle.buildFinished {
            Utils.prtln("----buildFinished--")
            def curN = "git branch".execute().text.replaceAll("\n", "").replaceAll(" ", "").replaceAll("\\*", "")
            Utils.prtln("curN>>: $curN")
            if (!curBranchName.equals(curN)) {
                Utils.prtln(" copy pre class error,checkout to $curBranchName buildFinished-")
                def gc = "git checkout -f $curBranchName".execute().waitFor()
                def gs = "git stash pop".execute().waitFor()
                Utils.prtln("finised checkout m gsp $gs")
            }
        }

        def dffe = "git diff ${aicConfig.preCommitId} --name-only".execute().text
        List<String> diffFiles = getDiffFiles(dffe)
        Utils.prtln("diff file list: ${diffFiles.toString()}")

        def subjectprojects = project.getRootProject().getSubprojects()

        "git add .".execute().waitFor()
        "git stash".execute().waitFor()
        def gcpr = "git checkout ${aicConfig.preCommitId}".execute().waitFor()
        Utils.prtln(" gcpr ${aicConfig.preCommitId} res: $gcpr")
        try {
            buildClass(isWindows)
            subjectprojects.each {
                Utils.prtln("moduleName ${it.name}")
                if (aicConfig.includeModule.contains(it.name)) {

                    def buildJavaDir = "${it.buildDir}${File.separator}intermediates${File.separator}javac${File.separator}debug${File.separator}classes"
                    def buildKotlinDir = "${it.buildDir}${File.separator}tmp${File.separator}kotlin-classes${File.separator}debug"

                    copyClass(it, buildJavaDir, buildKotlinDir, preJavaComDir, preKotlinComDir, diffFiles)
                }
            }

        } catch (Exception e) {
            Utils.prtln(" copy pre class error,checkout to $curBranchName ")
            def gc = "git checkout -f $curBranchName".execute().waitFor()
            def gs = "git stash pop".execute().waitFor()
            Utils.prtln("gsp $gs")
            throw new TaskExecutionException(this, e)
        }

        //切换分支
        if (isWindows) {
            "${project.rootDir}/gradlew clean".execute().waitFor()
        } else {
            "${project.rootDir}/gradlew clean".execute().waitFor()
        }

        // tttttttttttttttttttttt
        def gc = "git checkout $curBranchName".execute().waitFor()
        Utils.prtln(" gc $curBranchName res $gc")
        def gs = "git stash pop".execute().waitFor()
        Utils.prtln("gsp $gs")


        buildClass(isWindows)

        //切回之后copy 最新的class
        subjectprojects.each {
            if (aicConfig.includeModule.contains(it.name)) {

                def buildJavaDir = "${it.buildDir}${File.separator}intermediates${File.separator}javac${File.separator}debug${File.separator}classes"
                def buildKotlinDir = "${it.buildDir}${File.separator}tmp${File.separator}kotlin-classes${File.separator}debug"

                copyClass(it, buildJavaDir, buildKotlinDir, curJavaComDir, curKotlinComDir, diffFiles)

            }

        }

    }

    def copyClass(Project cproject, String buildJavaDir, String buildKotlinDir,
                  String destJavaDir, String destKotlinDir, List<String> difffiles
    ) {

        if (aicConfig.includeModule.contains(cproject.name)) {
            cproject.copy {
                from buildJavaDir
                into destJavaDir
                include difffiles
                exclude { details ->
                    (details.file.name == 'R.class' ||
                            details.file.name == 'R2.class' ||
                            details.file.name == 'BR.class' ||
                            details.file.name.startsWith('DataBind') ||
                            details.file.name.startsWith('R$') ||
                            details.file.name.endsWith("Binding.class") ||
                            details.file.name.startsWith('BuildConfig') ||
                            details.file.name.contains('$')
                    )
                }
            }

            cproject.copy {
                from buildKotlinDir
                into destKotlinDir
                exclude { details ->
                    (details.file.name.endsWith('.kotlin_module'))
                }
            }
        }
    }

    def excluedFilter(List<String> difffiles, String name) {
        return difffiles.contains(name.replace(".java"))
    }


    List<String> getDiffFiles(String diff) {
        List<String> diffFiles = new ArrayList<>()
        if (diff == null || diff == '') {
            return diffFiles
        }
        String[] strings = diff.split("\n")

        strings.each {
            if ((it.endsWith('.java') || it.endsWith('.kt')) && !it.contains("androidTest") && !it.contains("/src/test")) {
                if (isInclude(it)) {
                    int index = it.indexOf("src/main/java/")
                    if (index >= 0) {
                        try {
                            it = it.split("src/main/java/")[1]
                        } catch (Exception e) {
                            println('fk the error')
                        }
                    }
                    def ic = it.replace(".java", ".class")
                    diffFiles.add(ic)
                }
            }
        }
        return diffFiles
    }

    def isInclude(String javaPath) {
        List<String> includes = aicConfig.includes
        for (String packageName : includes) {
            String pkgNameTran = packageName.replaceAll("\\.", "/")
            if (javaPath.indexOf(pkgNameTran) >= 0) {
                return true
            }
        }
        return false
    }

    def buildClass(boolean isWindows) {
        def cdg = project.tasks.findByName("doCusCli")
        if (cdg) {
            Utils.prtln("docuslli esc")
            cdg.exec()
        } else {
            Utils.prtln("build class not find docli")
            if (isWindows) {
                def gcd = "${project.rootDir}/gradlew compileDebugSources --rerun-tasks".execute().waitFor()
                Utils.prtln(" gdcd w res $gcd")
            } else {
                def gcd = "${project.rootDir}/gradlew --debug compileDebugSources".execute().waitFor()
                Utils.prtln("gdcd res $gcd")
            }
        }

    }

}


