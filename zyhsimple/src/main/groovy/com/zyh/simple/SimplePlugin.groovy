package com.zyh.simple

import org.gradle.api.Plugin
import org.gradle.api.Project


class SimplePlugin implements Plugin<Project> {
   def  TAG = '---simplePlugin------->'
    @Override
    void apply(Project project) {
        println "------===================---->  simplePlugin apply $project"
    }
}