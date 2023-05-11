import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import org.gradle.api.Project

class SimpleTransform extends Transform {
    Project mProject
    SimpleTransform(Project project) {
        mProject = project
    }

    @Override
    String getName() {
        return "SimpleTransform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {//指定处理的文件类型
        return TransformManager.CONTENT_CLASS // .class
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {//指定输入文件范围，
        return TransformManager.SCOPE_FULL_PROJECT // 所以project
    }

    @Override
    boolean isIncremental() {//是否支持增量编译
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
       //父类空实现
//        super.transform(transformInvocation)
        println 'simpleTransform transform called'
        //实现对应逻辑
    }
}