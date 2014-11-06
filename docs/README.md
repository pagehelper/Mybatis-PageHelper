#Mybatis分页插件 - PageHelper说明   

如果你也在用Mybatis，建议尝试该分页插件，这个一定是<b>最方便</b>使用的分页插件。  

该插件目前支持`Oracle`,`Mysql`,`Hsqldb`,`PostgreSQL`四种数据库分页。  

项目文档：  

 1. [如何使用分页插件](HowToUse.md)  
 
 2. [重要提示](Important.md)
 
 3. [分页插件项目测试](Test.md) 
 
 4. [更新日志](Changelog.md) 
 
 5. [单文件文档](Single.md)

 6. [点击提交(gitosc)BUG](http://git.oschina.net/free/Mybatis_PageHelper/issues/new?issue%5Bassignee_id%5D=&issue%5Bmilestone_id%5D=)

<br>
<br>
<br>

##最新稳定版为3.2.3 版
  
3.2.3版本使用方法请切换到3.2.3版标签查看

地址：[点击进入gitosc-3.2.3目录](http://git.oschina.net/free/Mybatis_PageHelper/tree/v3.2.3/) | [点击进入github-3.2.3目录](https://github.com/pagehelper/Mybatis-PageHelper/tree/v3.2.3/) 

<br>
<br>
<br>

#最新测试版3.3.0-SNAPSHOT

##重要提示：`fdb-sql-parser`换为`jsqlparser`  

为了去掉count查询中的order by语句，最早使用了`fdb-sql-parser`，由于效果不好，现在已经替换成`jsqlparser`，`jsqlparser`比`fdb-sql-parser`更通用，而且体积更小，对原sql改动更少。替换后，下面的有关说明都会改为`jsqlparser`，如果你使用了最新的测试版分页，你需要下载`jsqlparser`。  

##3.3.0-SNAPSHOT改进内容

 1. 对`MappedStatement`对象进行缓存，包括count查询的`MappedStatement`以及分页查询的`MappedStatement`，分页查询改为预编译查询。

 2. 独立的`SqlUtil`类，由于原来的`PageHelper`太复杂，因此将拦截器外的其他代码独立到`SqlUtil`中，方便查看代码和维护。`SqlUtil`中增加`Parser`接口，提供一个抽象的`SimpleParser`实现，不同数据库的分页代码通过继承`SimpleParser`实现。

 3. 特殊的`Parser`实现类`SqlParser`类，这是一个独立的java类，主要提供了更高性能的count查询sql，可以根据sql自动改为`count(*)`查询，自动去除不需要的`order by`语句，如果需要使用该类，只要把该类放到`SqlUtil`类相同的包下即可，同时需要引入Jar包`jsqlparser-0.9.1.jar`。

 4. 增强的`PageInfo`类，`PageInfo`类包含了分页几乎所有需要用到的属性值，减少了对分页逻辑的过多投入。  

 4. 分页合理化，自动处理pageNum的异常情况。例如当pageNum<=0时，会设置pageNum=1，然后查询第一页。当pageNum>pages(总页数)时，自动将pageNum=pages，查询最后一页。  

 5. 增加对`PostgreSQL`支持。

<br>
<br>
<br>

##相关链接

对应于oschub的项目地址：http://git.oschina.net/free/Mybatis_PageHelper

对应于github的项目地址：https://github.com/pagehelper/Mybatis-PageHelper

Mybatis-Sample（分页插件测试项目）：[http://git.oschina.net/free/Mybatis-Sample](http://git.oschina.net/free/Mybatis-Sample)

Mybatis项目：https://github.com/mybatis/mybatis-3

Mybatis文档：http://mybatis.github.io/mybatis-3/zh/index.html  

Mybatis专栏： 

- [Mybatis示例](http://blog.csdn.net/column/details/mybatis-sample.html)

- [Mybatis问题集](http://blog.csdn.net/column/details/mybatisqa.html)  

作者博客：  

- http://my.oschina.net/flags/blog

- http://blog.csdn.net/isea533   

作者QQ： 120807756  

作者邮箱： abel533@gmail.com  

推荐一个Mybatis的QQ群： 146127540  
