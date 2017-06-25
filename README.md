# MyBatis Pagination - PageHelper

[![Build Status](https://travis-ci.org/pagehelper/Mybatis-PageHelper.svg?branch=master)](https://travis-ci.org/pagehelper/Mybatis-PageHelper)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/com.github.pagehelper/pagehelper/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.pagehelper/pagehelper)
[![Dependency Status](https://www.versioneye.com/user/projects/5932123f22f278003c5f851e/badge.svg?style=flat)](https://www.versioneye.com/user/projects/5932123f22f278003c5f851e)

[中文版文档](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/README_zh.md)

If you are using MyBatis, it is recommended to try this pagination plugin. 
This must be the **Most Convenient** pagination plugin.

PageHelper support any complex single-table, multi-table queries.
Some special cases please see the [**Important note**](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/en/Important.md).

Want to use PageHelper? 
Please see [**How to use PageHelper**](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/en/HowToUse.md).

## 新书《MyBatis 从入门到精通》

![MyBatis 从入门到精通](https://github.com/mybatis-book/book/raw/master/book.png)

预售地址：[京东](https://item.jd.com/12103309.html)，[当当](http://product.dangdang.com/25098208.html)，[亚马逊](https://www.amazon.cn/MyBatis从入门到精通-刘增辉/dp/B072RC11DM/ref=sr_1_18?ie=UTF8&qid=1498007125&sr=8-18&keywords=mybatis)

CSDN博客：http://blog.csdn.net/isea533/article/details/73555400

GitHub项目：https://github.com/mybatis-book/book

## Support [MyBatis 3.1.0+](https://github.com/mybatis/mybatis-3)
## Physical Paging

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

## Use [QueryInterceptor spec](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/src/main/java/com/github/pagehelper/QueryInterceptor.java) 

## Installation

To use PageHelper you just need to include the 
[pagehelper-x.x.x.jar](http://repo1.maven.org/maven2/com/github/pagehelper/pagehelper/) 
and [jsqlparser-0.9.5.jar](http://repo1.maven.org/maven2/com/github/jsqlparser/jsqlparser/0.9.5/) file in the classpath.

If you are using Maven just add the following dependency to your pom.xml:

```xml  
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper</artifactId>
    <version>latest version</version>
</dependency>
```  

If you are using Spring Boot, You can refer to [pagehelper-spring-boot-starter](https://github.com/pagehelper/pagehelper-spring-boot)

[Read More...](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/en/HowToUse.md)

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

## Author Info
Web: http://www.mybatis.tk

Blog: http://blog.csdn.net/isea533

Email: abel533@gmail.com  

QQ Group(recommend): <a target="_blank" href="http://shang.qq.com/wpa/qunwpa?idkey=7c2f018e4cddc7d4aad04fc312b2d69361a0a896a4f59219a7914953a57bffc2"><img border="0" src="http://pub.idqqimg.com/wpa/images/group.png" alt="Mybatis工具群(2)" title="Mybatis工具群(2)"></a>

QQ Group(2000 full): <a target="_blank" href="http://shang.qq.com/wpa/qunwpa?idkey=29e4cce8ac3c65d14a1dc40c9ba5c8e71304f143f3ad759ac0b05146e0952044"><img border="0" src="http://pub.idqqimg.com/wpa/images/group.png" alt="Mybatis工具" title="Mybatis工具"></a>

PageHelper on github:https://github.com/pagehelper/Mybatis-PageHelper

PageHelper on gitosc:http://git.oschina.net/free/Mybatis_PageHelper