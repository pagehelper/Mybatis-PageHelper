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

##最新版本为3.6.4

###Maven坐标

```xml  
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper</artifactId>
    <version>3.6.4</version>
</dependency>
```  

###下载JAR包

分页插件pagehelper.jar： 

 - https://oss.sonatype.org/content/repositories/releases/com/github/pagehelper/pagehelper/
 
 - http://repo1.maven.org/maven2/com/github/pagehelper/pagehelper/

由于使用了sql解析工具，你还需要下载jsqlparser.jar（这个文件完全独立，不依赖其他）：  

 - http://repo1.maven.org/maven2/com/github/jsqlparser/jsqlparser/0.9.1/
 
 - http://git.oschina.net/free/Mybatis_PageHelper/attach_files

##3.6.4更新日志：

 - 重构，将原来的内部类全部独立出来，尤其是`Parser`接口以及全部实现。
   现在可以直接使用`Parser`，使用方法如下：

   ```java
   String originalSql = "Select * from country o where id > 10 order by id desc ";

   Parser parser = AbstractParser.newParser("mysql");
   //获取count查询sql
   String countSql = parser.getCountSql(originalSql);
   //获取分页sql，这种方式不适合sqlserver数据库
   String pageSql = parser.getPageSql(originalSql);

   //sqlserver用下面的方法
   SqlServer sqlServer = new SqlServer();
   pageSql = sqlServer.convertToPageSql(originalSql, 1, 10);
   ```

##3.6.3更新日志：

 - 解决了一个潜在的bug，对[通用Mapper](http://git.oschina.net/free/Mapper)中的`SqlMapper`进行分页时，需要使用这个版本
 
##3.6.2更新日志：

 - 本次更新只是增加了一个异常提示，当<b>错误</b>的配置了多个分页插件时，会有更友好的错误提示：
 
   >分页插件配置错误:请不要在系统中配置多个分页插件(使用Spring时,mybatis-config.xml和Spring<bean>配置方式，请选择其中一种，不要同时配置多个分页插件)！


##3.6.1更新日志：

 - 解决select distinct导致count查询结果不正确的bug#35
 
 - 完善测试

##3.6.0更新日志：

 - 支持db2数据库
 
 - 支持sqlserver(2005+)数据库
 
 - sqlserver注意事项： 
   - 请先保证你的SQL可以执行
   - sql中最好直接包含order by，可以自动从sql提取
   - 如果没有order by，可以通过入参提供，但是需要自己保证正确
   - 如果sql有order by，可以通过orderby参数覆盖sql中的order by
   - order by的列名不能使用别名(`UNION,INTERSECT,MINUS,EXCEPT`等复杂sql不受限制，具体可以自己尝试)
   - 表和列使用别名的时候不要使用单引号(')

 - 简单修改结构
 
 - `startPage`方法返回值从`void`改为`Page`，获取`Page`后可以修改参数值
 
 - `Page`增加一个针对sqlserver的属性`orderBy`，用法看上面的<b>注意事项</b>
 
 - `Page`增加了一个链式赋值的方法，可以像下面这样使用：
   `PageHelper.startPage(1,10).count(false).reasonable(true).pageSizeZero(false)`
   
 - `PageHelper`增加了`startPage(int pageNum, int pageSize,String orderBy)`方法，针对sqlserver

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