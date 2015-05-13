#Mybatis分页插件 - PageHelper   

如果你也在用Mybatis，建议尝试该分页插件，这一定是<b>最方便</b>使用的分页插件。  

该插件目前支持以下数据库的<b>物理分页</b>:

 1. `Oracle`
 2. `Mysql`
 3. `MariaDB`
 4. `SQLite`
 5. `Hsqldb`
 6. `PostgreSQL`
 7. `DB2`
 8. `SqlServer(2005+)`
 9. `Informix`

##最新版本为3.7.2

###Maven坐标

```xml  
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper</artifactId>
    <version>3.7.2</version>
</dependency>
```  

###下载JAR包

分页插件pagehelper.jar： 

 - https://oss.sonatype.org/content/repositories/releases/com/github/pagehelper/pagehelper/
 
 - http://repo1.maven.org/maven2/com/github/pagehelper/pagehelper/

由于使用了sql解析工具，你还需要下载jsqlparser.jar（这个文件完全独立，不依赖其他）：  

 - http://repo1.maven.org/maven2/com/github/jsqlparser/jsqlparser/0.9.1/
 
 - http://git.oschina.net/free/Mybatis_PageHelper/attach_files

##3.7.2更新日志：

 - jsqlparser解析sql会抛出Error异常，由于直接只捕获Exception，所以导致部分解析失败的sql无法使用嵌套方式处理，所以修改为捕获`Throwable`。

##3.7.1更新日志：

 - 增加`Informix`数据库支持，设置`dialect`值为`informix`即可
 - 解决入参为不可变`Map`类型时的错误

##3.7.0更新日志：

 - 由于`orderby`参数经常被错误认为的使用，因此该版本全面移除了`orderby`
 - `Page<E>`移除`orderby`属性
 - `PageHelper`的`startPage`方法中，移除包含`orderby`参数的方法，sqlserver相关包含该参数的全部移除
 - 对SqlServer进行分页查询时，请在sql中包含order by语句，否则会抛出异常
 - 当`offsetAsPageNum=false`的时候，由于PageNum问题，`RowBounds`查询的时候`reasonable`会强制为false，已解决
 - 少数情况下的select中包含单个函数查询时，会使用嵌套的count查询

##项目文档[wiki](http://git.oschina.net/free/Mybatis_PageHelper/wikis/home)：  

###&gt;[如何使用分页插件](http://git.oschina.net/free/Mybatis_PageHelper/blob/master/wikis/HowToUse.markdown)  

###&gt;[更新日志](http://git.oschina.net/free/Mybatis_PageHelper/blob/master/wikis/Changelog.markdown) 

###&gt;[提交(gitosc)BUG](http://git.oschina.net/free/Mybatis_PageHelper/issues/new?issue%5Bassignee_id%5D=&issue%5Bmilestone_id%5D=)

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

Mybatis工具群： 211286137 (Mybatis相关工具插件等等)