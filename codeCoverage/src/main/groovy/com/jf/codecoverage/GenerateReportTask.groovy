package com.jf.codecoverage

import com.android.utils.FileUtils
import com.jf.codecoverage.report.ReportGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.jacoco.core.diff.DiffAnalyzer

import java.lang.management.ManagementFactory

import static java.sql.DriverManager.println

class GenerateReportTask extends DefaultTask{
    @Input
    AICConfig aicConfig

    @TaskAction
    def  generateReport(){

        Utils.prtln("start generateReport-----------------")

        def  classDirLists = toFileList(["${project.projectDir.absolutePath}/classes"])
        Utils.prtln("generatereport re>>: ---------- ${reloadDiff()}  start ------- >>> ")
//         Utils.prtln("sourceDirectories List: ${aicConfig.sourceDirectories.toString()}")

        ReportGenerator generator = new ReportGenerator(aicConfig.execDir, toFileList(classDirLists),
                toFileList(aicConfig.sourceDirectories), new File(aicConfig.reportDirectory));
        generator.create()

    }

    def toFileList(List<String> path) {
        List<File> list = new ArrayList<>(path.size())
        for (String s : path)
            list.add(new File(s))
        return list
    }


    def reloadDiff() {
        try {
            if (DiffAnalyzer.getInstance().getDiffList().size() <= 0) {
                Utils.prtln("reload diff--------------")
                def preDir = "${project.rootDir.parentFile}${File.separator}${project.parent.name}classtmp${File.separator}pre"
                def currentDir = "${project.rootDir.parentFile}${File.separator}${project.parent.name}classtmp${File.separator}cur"
                DiffAnalyzer.getInstance().reset()
                DiffAnalyzer.readClasses(currentDir, DiffAnalyzer.CURRENT)
                DiffAnalyzer.readClasses(preDir, DiffAnalyzer.PRECOMMIT)
                DiffAnalyzer.getInstance().diff()
                writeDiffMethodToFile()
                true
            }
            false
        } catch (Exception e) {
            Utils.prtln(" generate read error:  ${e.getLocalizedMessage()}")
        }

    }

    def writeDiffMethodToFile() {
        try {
            String path = "${mProject.projectDir}${File.separator}classes${File.separator}diff${File.separator}diffMethod.txt"
            FileUtils.writeToFile(new File(path), DiffAnalyzer.getInstance().toString());
            println("writerDiffMethodToFile size=" + DiffAnalyzer.getInstance().getDiffList().size() + " >" + path)
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
