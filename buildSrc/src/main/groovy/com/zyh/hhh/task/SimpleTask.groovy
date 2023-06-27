import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction


class SimpleTask extends  DefaultTask{

    @TaskAction
    def doCusSth(){
        println 'do custom sth.'
        def path = "${project.buildDir}$File.separator/outputs${File.separator}/simplefolder"
        File f = new File(path)
        if(!f.exists()){
          f.mkdir()
        }

    }

    @Override
    Task doLast(Action<? super Task> action) {
        println 'do last  simple task '
        return super.doLast(action)
    }
}