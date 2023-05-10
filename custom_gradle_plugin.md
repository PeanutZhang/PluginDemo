### 自定义Gradle插件

tip: 之前了解过自定义插件，开发中用的比较少，基本上忘记了，

由于工作转到了自动化测试相关，自动化测试框架使用gradle和自定义插件的技术点比较多，所以重新学习补充gradle相关知识。

此文档记录自己学习思路和相关实现细节。实现方式先列出个目录大纲，然后逐个或者穿插学习

#### 1 是什么 

​     自定义插件是什么，什么场景下需要自定义插件

​     android studio 通过gradle和agp 编译项目，同样我们可以通过自定义插件在程序执行开始前做一些特定工作,同时可以hook gradle的生命周期做一些事，总之就是在项目运行开始前或者运行过程中做些前置逻辑等。

#### 2 如何做 

   如何开发自定义插件，需要哪些技能

##### 2.1 需要熟悉gradle基本概念和基础语法及一些自身的api

​    个人理解： gradle是一个自动化构建工具，使用groovy开发，因为是基于jvm的，所以可以直接在gradle 脚本中是java开发。通过task来执行相应的任务执行，同时也是一个编程语言可以在构建过程中做一些业务逻辑。

相关基础知识和学习资料网上不乏优秀学习资料可参考 [gradle学习路线](https://www.wanandroid.com/route/show/582)  [gradle初探](https://juejin.cn/post/7170684769083555877)

 这里提一个点gradle自身的生命周期，由于自动化构建过程和gradle插件都涉及到相对重要。

##### Gradle 生命周期（构建流程）

主要可以包含 初始化阶段， 配置阶段和执行阶段， 相对应的节点都有生命周期的回调，可以根据业务需要hook对应的构建流程。

1. initialization 初始化阶段 

   对应android项目中setting.gradle 文件，生成Gradle, Setting Project对象，

   这三个对象都有自己一些api,供开发者使用。

   ​	project: 对应每个模块下的build.gradle文件

2. configuration 配置阶段 

   先执行根目录下的 build.gradle ,顺序执行子模块的build.gradle文件生成一个有向无环图

3. execution 执行阶段 ，按照配置阶段生成的有向无环图顺序执行task

   

​	

#### 3 自己实现的demo

#### 4 小结









