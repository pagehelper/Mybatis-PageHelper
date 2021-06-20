![MyBatis Pagination - PageHelper](logo.png)

# MyBatis Pagination - PageHelper

[![Build Status](https://travis-ci.org/pagehelper/Mybatis-PageHelper.svg?branch=master)](https://travis-ci.org/pagehelper/Mybatis-PageHelper)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/com.github.pagehelper/pagehelper/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.pagehelper/pagehelper)

[中文版文档](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/README_zh.md)

If you are using MyBatis, it is recommended to try this pagination plugin. This must be the **MOST CONVENIENT**
pagination plugin.

PageHelper supports any complex single-table, multi-table queries.
As to some special cases, please refer to the [**Important note**](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/en/Important.md).

Want to use PageHelper? 
Please check out [**How to use PageHelper**](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/en/HowToUse.md).

## New JavaDoc

https://apidoc.gitee.com/free/Mybatis_PageHelper

APIs: https://apidoc.gitee.com/free/Mybatis_PageHelper/com/github/pagehelper/page/PageMethod.html

## 《MyBatis 从入门到精通》

![MyBatis 从入门到精通](https://github.com/mybatis-book/book/raw/master/book.png)

[京东](https://item.jd.com/12103309.html) ，[当当](http://product.dangdang.com/25098208.html) ，[Amazon](https://www.amazon.cn/MyBatis从入门到精通-刘增辉/dp/B072RC11DM/ref=sr_1_18?ie=UTF8&qid=1498007125&sr=8-18&keywords=mybatis)

CSDN Blog：http://blog.csdn.net/isea533/article/details/73555400

GitHub：https://github.com/mybatis-book/book

## Support [MyBatis 3.1.0+](https://github.com/mybatis/mybatis-3)
## Physical Paging

PageHelper supports the following databases [PageAutoDialect](src/main/java/com/github/pagehelper/page/PageAutoDialect.java):

```java
static {
    //register aliass
    registerDialectAlias("hsqldb", HsqldbDialect.class);
    registerDialectAlias("h2", HsqldbDialect.class);
    registerDialectAlias("phoenix", HsqldbDialect.class);
    registerDialectAlias("postgresql", PostgreSqlDialect.class);
    registerDialectAlias("mysql", MySqlDialect.class); 
    registerDialectAlias("mariadb", MySqlDialect.class); 
    registerDialectAlias("sqlite", MySqlDialect.class);
    registerDialectAlias("herddb", HerdDBDialect.class);
    registerDialectAlias("oracle", OracleDialect.class); 
    registerDialectAlias("oracle9i", Oracle9iDialect.class); 
    registerDialectAlias("db2", Db2Dialect.class); 
    registerDialectAlias("informix", InformixDialect.class); 
    //解决 informix-sqli #129，仍然保留上面的 
    registerDialectAlias("informix-sqli", InformixDialect.class);
    registerDialectAlias("sqlserver", SqlServerDialect.class); 
    registerDialectAlias("sqlserver2012", SqlServer2012Dialect.class);
    registerDialectAlias("derby", SqlServer2012Dialect.class); 
    //达梦数据库,https://github.com/mybatis-book/book/issues/43 
    registerDialectAlias("dm", OracleDialect.class); 
    //阿里云PPAS数据库,https://github.com/pagehelper/Mybatis-PageHelper/issues/281 
    registerDialectAlias("edb", OracleDialect.class); 
    //神通数据库 
    registerDialectAlias("oscar", OscarDialect.class); 
    registerDialectAlias("clickhouse", MySqlDialect.class); 
    //瀚高数据库 
    registerDialectAlias("highgo", HsqldbDialect.class); 
    //虚谷数据库 
    registerDialectAlias("xugu", HsqldbDialect.class); 
}
```

> If the database you are using is not in this list, you can configure the `dialectAlias` parameter.
>
> This parameter allows to configure the alias of a custom implementation, 
> which can be used to automatically obtain the corresponding implementation according to the JDBCURL, 
> and allows to overwrite the existing implementation in this way. 
> The configuration example is as follows (use semicolons to separate multiple alias):
>
>```xml
><property name="dialectAlias" value="oracle=com.github.pagehelper.dialect.helper.OracleDialect"/>
>```

## Use [QueryInterceptor spec](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/src/main/java/com/github/pagehelper/QueryInterceptor.java) 

## Installation

To use PageHelper, you just need to include the 
[pagehelper-x.y.z.jar](http://repo1.maven.org/maven2/com/github/pagehelper/pagehelper/)
and [jsqlparser-x.y.z.jar](http://repo1.maven.org/maven2/com/github/jsqlparser/jsqlparser/) file in the classpath.

> For version matching relation, please refer to the dependent version in pom.

If you are using Maven, you could just add the following dependency to your `pom.xml`:

```xml  
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper</artifactId>
    <version>latest version</version>
</dependency>
```  

If you are using Spring Boot, You can refer to the [pagehelper-spring-boot-starter](https://github.com/pagehelper/pagehelper-spring-boot)

[More...](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/en/HowToUse.md)

## Documentation  
- [How to use the PageHelper](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/en/HowToUse.md)
- [Changelog](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/en/Changelog.md)
- [Important note](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/en/Important.md)

## Spring integration sample
- [Integration Spring 3.x](https://github.com/abel533/Mybatis-Spring/tree/spring3.x)
- [Integration Spring 4.x](https://github.com/abel533/Mybatis-Spring)
- [Integration Spring Boot](https://github.com/abel533/MyBatis-Spring-Boot)

## Submit BUG
- [Submit to github](https://github.com/pagehelper/Mybatis-PageHelper/issues/new)
- [Submit to gitosc](http://git.oschina.net/free/Mybatis_PageHelper/issues/new?issue%5Bassignee_id%5D=&issue%5Bmilestone_id%5D=)

## Thanks for free JetBrains Open Source license

<a href="https://www.jetbrains.com/?from=Mybatis-PageHelper" target="_blank">
<img src="https://user-images.githubusercontent.com/1787798/69898077-4f4e3d00-138f-11ea-81f9-96fb7c49da89.png" height="200"/></a>

## Author Info
Web: https://mybatis.io

Blog: http://blog.csdn.net/isea533

Email: abel533@gmail.com

PageHelper on github:https://github.com/pagehelper/Mybatis-PageHelper

PageHelper on gitosc:http://git.oschina.net/free/Mybatis_PageHelper
