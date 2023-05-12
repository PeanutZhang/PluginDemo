### 自定义Gradle插件

> 实践过程基于gradle 6.5 ,  android.tools.build:gradle:4.1.3

 https://juejin.cn/post/7098383560746696718

​	https://docs.gradle.org/current/userguide/userguide.html

tip: 之前了解过自定义插件，开发中用的比较少，基本上忘记了，

由于工作转到了自动化测试相关，自动化测试框架使用gradle和自定义插件的技术点比较多，所以重新学习补充gradle相关知识。

此文档记录自己学习思路和相关实现细节。实现方式先列出个目录大纲，然后逐个或者穿插学习

#### 1 是什么 

​     自定义插件是什么，什么场景下需要自定义插件

​    ~~android studio 通过gradle和agp 编译项目，同样我们可以通过自定义插件在程序执行开始前做一些特定工作,    同时可以hook gradle的生命周期做一些事，总之就是在项目运行开始前或者运行过程中做些前置逻辑等。~~

Gradle 和 Gradle 插件是两个完全不同的概念，Gradle 提供的是一套核心的构建机制，而 Gradle 插件则是运行在这套机制上的一些具体构建逻辑，本质上和 .gradle 文件是相同。例如，我们熟悉的编译 Java 代码的能力，都是由插件提供的。

#### 2 如何做 

   2种实现方法：直接在脚本中实现脚本插件，只能一个项目使用；通过单独模块定义，依赖打包后的文件，复用性好

##### 2.1 需要熟悉gradle基本概念和基础语法及一些自身的api

​    个人理解： gradle是一个自动化构建工具，使用groovy开发，因为是基于jvm的，所以可以直接在gradle 脚本中是java开发。通过task来执行相应的任务执行，同时也是一个编程语言可以在构建过程中做一些业务逻辑。

相关基础知识和学习资料网上不乏优秀学习资料可参考 [gradle学习路线](https://www.wanandroid.com/route/show/582)  [gradle初探](https://juejin.cn/post/7170684769083555877)

 这里提一个点gradle自身的生命周期，由于自动化构建过程和gradle插件都涉及到相对重要。

##### 2.2 Gradle 生命周期（构建流程）

主要可以包含 初始化阶段， 配置阶段和执行阶段， 相对应的节点都有生命周期的回调，可以根据业务需要hook对应的构建流程。

1. initialization 初始化阶段 

   对应android项目中setting.gradle 文件，生成Gradle, Setting Project对象，

   这三个对象都有自己一些api,供开发者使用。

   ​	project: 对应每个模块下的build.gradle文件

2. configuration 配置阶段 

   先执行根目录下的 build.gradle ,顺序执行子模块的build.gradle文件生成一个有向无环图

3. execution 执行阶段 ，按照配置阶段生成的有向无环图顺序执行task

   ​	

##### 2.3 transform 

[transform](https://juejin.cn/post/7098752199575994405)

##### 3 自动化发布

本地仓库或者远程仓库或者私服

[参考](https://juejin.cn/post/6963633839860088846)

###### 前言

######  什么是 POM？

POM（Project Object Model）指项目对象模型，用于描述项目构件的基本信息。一个有效的 POM 节点中主要包含一下信息：

| 配置                    | 描述              | 举例（'com.github.bumptech.glide:glide:4.11.0'） |
| ----------------------- | ----------------- | ------------------------------------------------ |
| groupId                 | 组织 / 公司的名称 | com.github.bumptech.glide                        |
| artifactId([ˈɑːtɪfækt]) | 组件的名称        | glide                                            |
| version                 | 组件的版本        | 4.11.0                                           |
| packaging               | 打包的格式        | aar                                              |

######   什么是仓库（repository）？

在项目中，我们会需要依赖各种各样的二方库或三方库，这些依赖一定会存放在某个位置（Place），这个 “位置” 就叫做仓库。使用仓库可以帮助我们管理项目构件，例如 jar、aar 等等

**主流的构建工具都有三个层次的仓库概念：**

- **1、本地仓库：** 无论使用 Linux 还是 Window，计算机中会有一个目录用来存放从中央仓库或远程仓库下载的依赖文件；
- **2、中央仓库：** 开源社区提供的仓库，是绝大多数开源库的存放位置。比如 Maven 社区的中央仓库 [Maven Central](https://link.juejin.cn?target=https%3A%2F%2Fsearch.maven.org%2F)；
- **3、私有仓库：** 公司或组织的自定义仓库，可以理解为二方库的存放位置。

主要脚本

```
plugins {
    id 'groovy' // Groovy Language
    id 'maven'
    id 'java-gradle-plugin'
}
dependencies {
    implementation gradleApi()
    implementation localGroovy()
}

gradlePlugin {
    plugins {
        fkPu {
            id  = 'com.zyh.simple'
            implementationClass  = 'com.zyh.simple.SimplePlugin'

        }
    }
}

def NEXUS_REPOSITORY_URL="http://localhost:8081/nexus3/repository/maven-releases/"
def NEXUS_USERNAME="admin"
def NEXUS_PASSWORD="123456"

uploadArchives {
    repositories {
        mavenDeployer {
            pom.groupId = 'com.zyh.simple'
            pom.artifactId = 'simplePlugin'
            pom.version = '1.5.0'
            //发布到本地仓库
            repository(url: uri('../localMavenRepository'))
            //github 
            //或者nuxe私服
            
            //  repository(url: NEXUS_REPOSITORY_URL) {
//                authentication(userName: NEXUS_USERNAME, password: NEXUS_PASSWORD)
//            }
        }
    }
}

使用：：===============
//根目录build.gradle
 repositories {
      
        maven { url "localMavenRepository" }
    }
    dependencies {
    	        classpath "com.zyh.simple:simplePlugin:1.5.0"
    }
    plugins {
    	id 'com.zyh.simple'
    }

```

###### 3.1 发布到github

 3.1.1  root build.gradle

```
buildscript {
    repositories {
       //other center
       j
       //github
        maven { url 'https://jitpack.io' }
//        maven { url "localMavenRepository" }
    }

dependencies {
    ...
    classpath "com.github.dcendents:android-maven-gradle-plugin:1.5" // // GitHub Maven 插件
}
```

在发布的模块 build.gradle

````
plugins {
    id 'groovy' // Groovy Language
//    id 'maven'
    id 'maven-publish'
    id 'java-gradle-plugin'
    id 'com.github.dcendents.android-maven'//github maven 插件
}
//apply plugin:"com.github.dcendents.android-maven"
dependencies {
    implementation gradleApi()
    implementation localGroovy()
}

gradlePlugin {
    plugins {
        fkPu {
            id  = 'com.zyh.simple'
            implementationClass  = 'com.zyh.simple.SimplePlugin'

        }
    }
}

group = 'com.github.PeanutZhang'

//uploadArchives {
//    repositories {
//        mavenDeployer {
//            pom.groupId = 'com.zyh.simple'
//            pom.artifactId = 'simplePlugin'
//            pom.version = '1.5.0'
//            repository(url: uri('../localMavenRepository'))
//        }
//    }
//}

````

//gradle 6.6+  use  maven-publish

```
group 'com.ttp.and_jacoco2'
version '1.1.0'
// maven-publish
publishing {
    publications {
        mavenJava(MavenPublication) {
            from components.java
        }
    }
    repositories {
        maven {
            url = uri('../repo')
        }
    }
}
```





发布github

1. 把项目就push上去
2. 打一个tag  v1.0.0
2.  在https://jitpack.io/ 输入项目地址 点击look up ，成功之后，在下方 选择对应的tag ，点击get,稍等后便可以使用
2. ![image-20230511091511796](/home/zyh/.config/Typora/typora-user-images/image-20230511091511796.png)

```

```



#### 3 自己实现的demo

#### 4 小结









