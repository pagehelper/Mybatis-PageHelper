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
 8. `SqlServer(2005+)`
 9. `Informix`

配置`dialect`属性时，使用小写形式：

`oracle`,`mysql`,`mariadb`,`sqlite`,`hsqldb`,`postgresql`,`db2`,`sqlserver`,`informix`

##MyBatis工具网站:[http://mybatis.tk](http://www.mybatis.tk)

##分页插件支持MyBatis3.2.0~3.3.0(包含)

##分页插件最新版本为4.0.0

###Maven坐标

```xml  
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper</artifactId>
    <version>4.0.0</version>
</dependency>
```  

###下载JAR包

分页插件pagehelper.jar： 

 - https://oss.sonatype.org/content/repositories/releases/com/github/pagehelper/pagehelper/
 
 - http://repo1.maven.org/maven2/com/github/pagehelper/pagehelper/

由于使用了sql解析工具，你还需要下载jsqlparser.jar（这个文件完全独立，不依赖其他）：  

 - http://repo1.maven.org/maven2/com/github/jsqlparser/jsqlparser/0.9.1/
 
 - http://git.oschina.net/free/Mybatis_PageHelper/attach_files

##4.0.0更新日志：

 - 配置属性`dialect`不在强制要求，可以不写，分页插件会自动判断

 - 解决从request中获取分页参数时的错误,感谢<b>探路者☆</b>

 - `PageInfo`增加空构造方法，所有属性增加`setter`方法

 - 增加对排序的支持

 - 可以单独使用`PageHelper.orderBy(String orderBy)`对查询语句增加排序，也可以配合`startPage`的其他方法使用

 - 可以使用`PageHelper.startPage(int start,int size,String orderBy)`对分页查询进行排序

 - 修改分页查询的处理逻辑，主要是将原`sqlSource`包装成可以分页和排序的`sqlSource`

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

作者QQ： 120807756  

作者邮箱： abel533@gmail.com  

Mybatis工具群： <a target="_blank" href="http://shang.qq.com/wpa/qunwpa?idkey=29e4cce8ac3c65d14a1dc40c9ba5c8e71304f143f3ad759ac0b05146e0952044"><img border="0" src="http://pub.idqqimg.com/wpa/images/group.png" alt="Mybatis工具" title="Mybatis工具"></a>