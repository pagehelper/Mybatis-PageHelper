#Mybatis分页插件 - PageHelper

如果你也在用Mybatis，建议尝试该分页插件，这一定是<b>最方便</b>使用的分页插件。

分页插件支持任何复杂的单表、多表分页，部分特殊情况请看[重要提示](http://git.oschina.net/free/Mybatis_PageHelper/blob/master/wikis/Important.markdown)。

想要使用分页插件？请看[如何使用分页插件](http://git.oschina.net/free/Mybatis_PageHelper/blob/master/wikis/HowToUse.markdown)。

##物理分页

该插件目前支持以下数据库的<b>物理分页</b>:

 1. `Oracle`
 2. `Mysql`
 3. `MariaDB`
 4. `SQLite`
 5. `Hsqldb`
 6. `PostgreSQL`
 7. `DB2`
 8. `SqlServer(2005,2008)`
 9. `Informix`
 10. `H2`
 11. `SqlServer2012`

配置`dialect`属性时，可以使用小写形式：

`oracle`,`mysql`,`mariadb`,`sqlite`,`hsqldb`,`postgresql`,`db2`,`sqlserver`,`informix`,`h2`,`sqlserver2012`

在4.0.0版本以后，`dialect`参数可以不配置，系统能自动识别这里提到的所有数据库。

对于不支持的数据库，可以实现`com.github.pagehelper.parser.Parser`接口，然后配置到`dialect`参数中(4.0.2版本增加)。

<b>特别注意：</b>使用SqlServer2012数据库时，需要手动指定`sqlserver2012`，否则会使用2005的方式进行分页。

##MyBatis工具网站:[http://mybatis.tk](http://www.mybatis.tk)

##分页插件支持MyBatis3.2.0~3.3.0(包含)

##分页插件最新版本为4.1.3

###Maven坐标

```xml  
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper</artifactId>
    <version>4.1.3</version>
</dependency>
```  

###下载JAR包

分页插件pagehelper.jar： 

 - https://oss.sonatype.org/content/repositories/releases/com/github/pagehelper/pagehelper/
 
 - http://repo1.maven.org/maven2/com/github/pagehelper/pagehelper/

###由于使用了sql解析工具，你还需要下载jsqlparser.jar

####4.1.0及以后版本需要0.9.4版本

 - http://repo1.maven.org/maven2/com/github/jsqlparser/jsqlparser/0.9.4/

####4.1.0以前版本需要0.9.1版本

 - http://repo1.maven.org/maven2/com/github/jsqlparser/jsqlparser/0.9.1/

##4.1.3更新日志

- 解决反射类没有完全捕获异常的问题#94
- 把SqlServer类所有private都改成了protected，方便继承修改#93

##4.1.2更新日志

- 增加可配参数`closeConn`，当使用动态数据源时，分页插件获取jdbcUrl后，控制是否关闭当前连接，默认`true`关闭
- count查询改为`count(0)`，分库分表情况下的效率可能更高

##4.1.1更新日志：

- 解决动态数据源时获取连接后未关闭的严重bug#80
- 解决动态数据源时SqlSource和parser绑定导致不能切换方言的问题

##4.1.0更新日志：

- 增加`autoRuntimeDialect`参数，允许在运行时根据多数据源自动识别对应方言的分页（暂时不支持自动选择`sqlserver2012`，只能使用`sqlserver`）。
- 去掉了4.0.3版本增加的`returnPageInfo`参数，接口返回值不支持`PageInfo`类型，可以使用下面`ISelect`中演示的方法获取
- 增加对`SqlServer2012`的支持，需要手动指定`dialect=sqlserver2012`，否则会使用2005的方式进行分页
- jsqlparser升级到0.9.4版本，使用jar包时必须用最新的0.9.4版本，使用Maven会自动依赖0.9.4
- 增加`ISelect`接口，方便调用，使用方法可以参考`src/test/java/com.github.pagehelper.test.basic.TestISelect`测试。

##项目文档[wiki](http://git.oschina.net/free/Mybatis_PageHelper/wikis/home)：  

###[如何使用分页插件](http://git.oschina.net/free/Mybatis_PageHelper/blob/master/wikis/HowToUse.markdown)

如果要使用分页插件，这篇文档一定要看，看完肯定没有问题。

如果和Spring集成不熟悉，可以参考下面两个MyBatis和Spring集成的框架

<b>只有基础的配置信息，没有任何现成的功能，作为新手入门搭建框架的基础</b>

- [集成Spring3.x](https://github.com/abel533/Mybatis-Spring)

- [集成Spring4.x](https://github.com/abel533/Mybatis-Spring/tree/spring4)

这两个集成框架集成了MyBatis分页插件和MyBatis通用Mapper。

###[如何使用排序插件](http://git.oschina.net/free/Mybatis_PageHelper/blob/master/wikis/UseOrderBy.md)

###[更新日志](http://git.oschina.net/free/Mybatis_PageHelper/blob/master/wikis/Changelog.markdown)

包含全部的详细的更新日志。

###[重要提示](http://git.oschina.net/free/Mybatis_PageHelper/blob/master/wikis/Important.markdown)

提示很重要，建议一定看一遍！

###[提交(gitosc)BUG](http://git.oschina.net/free/Mybatis_PageHelper/issues/new?issue%5Bassignee_id%5D=&issue%5Bmilestone_id%5D=)

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

作者邮箱： abel533@gmail.com  

Mybatis工具群： <a target="_blank" href="http://shang.qq.com/wpa/qunwpa?idkey=29e4cce8ac3c65d14a1dc40c9ba5c8e71304f143f3ad759ac0b05146e0952044"><img border="0" src="http://pub.idqqimg.com/wpa/images/group.png" alt="Mybatis工具" title="Mybatis工具"></a>