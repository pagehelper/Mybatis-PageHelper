#MyBatis Pagination - PageHelper

[中文版文档](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/README_zh.md)

If you are using MyBatis, it is recommended to try this pagination plugin. 
This must be the **Most Convenient** pagination plugin.

PageHelper supports any single table, multiple tables of complex pagination.
Some special cases please see the [**Important note**](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/Important.md).

Want to use PageHelper? 
Please see [**How to use PageHelper**](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/HowToUse.md).

##Physical Paging

PageHelper supports the following databases:

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
 12. `Derby`

PageHelper will detect the current database url by default, 
automatically select the corresponding database dialect.

You can configure `helperDialect` Property to specify the dialect.
You can use the following abbreviations :

`oracle`, `mysql`, `mariadb`, `sqlite`, `hsqldb`, `postgresql`,
`db2`, `sqlserver`, `informix`, `h2`, `sqlserver2012`, `derby`

Or You can use the dialect full name, such as `com.github.pagehelper.dialect.helper.MySqlDialect`.

**Special note :** When using the SqlServer2012 database,
you need to manually specify for `sqlserver2012`, otherwise it will use the SqlServer2005 for paging.

##MyBatis Tools:[http://mybatis.tk](http://www.mybatis.tk)
##mybatis-3 https://github.com/mybatis/mybatis-3 
##Support MyBatis 3.1.0+

##PageHelper 5.0
Due to pagination plugin version 5.0 and 4.2. X completely different,
so the master branch is 5.X version.
version 4.2 exists as a branch, 
if you have PR for 4.2, please submitted to the branch.

##Latest SNAPSHOT: 5.0.0-SNAPSHOT

##Latest Release 4.2.1

If you are using Maven just add the following dependency to your pom.xml:

```xml  
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper</artifactId>
    <version>4.2.1</version>
</dependency>
```  

##Latest Changelog

###4.2.1

- 解决`SimpleCache`类遗留问题导致的错误 [#143](http://git.oschina.net/free/Mybatis_PageHelper/issues/143) fix by [dhhua](https://github.com/dhhua)

###4.2.0

- 使用新的方式进行分页，4.2版本是从5.0版本分离出来的一个特殊版本，这个版本兼容4.x的所有功能，5.0版本时为了简化分页逻辑，会去掉部分功能，所以4.2是4.x的最后一个版本。
- 支持 MyBatis 3.1.0+ 版本
- 增加对 Derby 数据库的支持
- 对除 informix 外的全部数据库进行测试，全部通过
- PageHelper增加手动清除方法`clearPage()`
- 解决 SqlServer 多个`with(nolock)`时出错的问题
- 对CountMappedStatement 进行缓存，配置方式见BaseSqlUtil 319行
- 由于SqlServer的sql处理特殊，因此增加了两个SQL缓存，具体配置参考SqlServerDialect类
- 添加 sqlserver 别名进行排序功能，在解析sql时，会自动将使用的别名转换成列名 by panmingzhi
- 新增`sqlCacheClass`参数，该参数可选，可以设置sql缓存实现类，默认为`SimpleCache`，当项目包含guava时，使用`GuavaCache`，也可以通过参数`sqlCacheClass`指定自己的实现类，有关详情看`com.github.pagehelper.cache`包。
- 解决#135，增加/*keep orderby*/注解，SQL中包含该注释时，count查询时不会移出order by
- sqlserver没有orderby时，使用`order by rand()` #82 #118

##Documentation  

###[How to use the PageHelper](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/HowToUse.md)

If you are unfamiliar with Spring integration, 
you can refer to the following two MyBatis and Spring Integration Framework:

- [Integration Spring 3.x](https://github.com/abel533/Mybatis-Spring/tree/spring3.x)
- [Integration Spring 4.x](https://github.com/abel533/Mybatis-Spring)

The two Integrated Framework integrates PageHelper and [Common Mapper](https://github.com/abel533/Mapper)。

###How to use in simple RowBounds style

###Implement your own pagination plugin
- helperDialect style
- dialect style

##[Changelog](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/Changelog.md)

##[Important note](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/Important.md)

##Submit BUG

- [Submit to github](https://github.com/pagehelper/Mybatis-PageHelper/issues/new)
- [Submit to gitosc](http://git.oschina.net/free/Mybatis_PageHelper/issues/new?issue%5Bassignee_id%5D=&issue%5Bmilestone_id%5D=)

##Author Info

Blog:http://blog.csdn.net/isea533

Email: abel533@gmail.com  

QQ Group: <a target="_blank" href="http://shang.qq.com/wpa/qunwpa?idkey=29e4cce8ac3c65d14a1dc40c9ba5c8e71304f143f3ad759ac0b05146e0952044"><img border="0" src="http://pub.idqqimg.com/wpa/images/group.png" alt="MyBatis" title="MyBatis"></a>

PageHelper on github:https://github.com/pagehelper/Mybatis-PageHelper

PageHelper on gitosc:http://git.oschina.net/free/Mybatis_PageHelper