#MyBatis Pagination - PageHelper

[中文版文档](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/README_zh.md)

If you are using MyBatis, it is recommended to try this pagination plugin. 
This must be the **Most Convenient** pagination plugin.

PageHelper support any complex single-table, multi-table queries.
Some special cases please see the [**Important note**](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/en/Important.md).

Want to use PageHelper? 
Please see [**How to use PageHelper**](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/en/HowToUse.md).

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

##Latest Release 5.0.0-beta

To use PageHelper you just need to include the 
[pagehelper-x.x.x.jar](http://repo1.maven.org/maven2/com/github/pagehelper/pagehelper/) 
and [jsqlparser-0.9.5.jar](http://repo1.maven.org/maven2/com/github/jsqlparser/jsqlparser/0.9.5/) file in the classpath.

If you are using Maven just add the following dependency to your pom.xml:

```xml  
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper</artifactId>
    <version>5.0.0-beta</version>
</dependency>
```  

##Latest Changelog

###5.0.0-rc

- fix bug [#149](http://git.oschina.net/free/Mybatis_PageHelper/issues/149)
- renamed Db2RowDialect to Db2RowBoundsDialect
- All thrown exceptions being replaced by PageException
- Update Tutorials

###5.0.0-beta

- Use a better way to handle paging logic
- New pagination plugin interceptor `com.github.pagehelper.PageInterceptor`
- New `Dialect` `PageHelper` is a special implementation class, the previous function is implemented in more user-friendly ways
- New pagination plugin only a `dialect` parameter, the default `dialect` is `PageHelper`
- `PageHelper` continue to support previously provided parameters, Among the latest to use the document has been fully updated
- `PageHelper` has a `helperDialect` parameter which is the same functional as the previous `dialect`
- Added paging implementation based on pure `RowBounds` and `PageRowBounds`, 
in `com.github. pagehelper. dialect. rowbounds` package, it is used as `dialect` Parameter sample implementation, more detailed documentation will be added later
- Removed inappropriate orderby functions that appear in pagination plugin. It will provide a separate sort plug-ins in the future
- Remove `PageHelper` are less commonly used methods
- A new document, an important part of the update has been mentioned in the changelog, provides the English version of this document

##Documentation  

###[How to use the PageHelper](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/en/HowToUse.md)

If you are unfamiliar with Spring integration, 
you can refer to the following two MyBatis and Spring Integration Framework:

- [Integration Spring 3.x](https://github.com/abel533/Mybatis-Spring/tree/spring3.x)
- [Integration Spring 4.x](https://github.com/abel533/Mybatis-Spring)

The two Integrated Framework integrates PageHelper and [Common Mapper](https://github.com/abel533/Mapper)。

###How to use in simple RowBounds style

###Implement your own pagination plugin
- helperDialect style
- dialect style

##[Changelog](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/en/Changelog.md)

##[Important note](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/en/Important.md)

##Submit BUG

- [Submit to github](https://github.com/pagehelper/Mybatis-PageHelper/issues/new)
- [Submit to gitosc](http://git.oschina.net/free/Mybatis_PageHelper/issues/new?issue%5Bassignee_id%5D=&issue%5Bmilestone_id%5D=)

##Author Info

Blog:http://blog.csdn.net/isea533

Email: abel533@gmail.com  

QQ Group: <a target="_blank" href="http://shang.qq.com/wpa/qunwpa?idkey=29e4cce8ac3c65d14a1dc40c9ba5c8e71304f143f3ad759ac0b05146e0952044"><img border="0" src="http://pub.idqqimg.com/wpa/images/group.png" alt="MyBatis" title="MyBatis"></a>

PageHelper on github:https://github.com/pagehelper/Mybatis-PageHelper

PageHelper on gitosc:http://git.oschina.net/free/Mybatis_PageHelper