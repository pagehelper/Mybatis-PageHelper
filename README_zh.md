# MyBatis 分页插件 - PageHelper

[![Build Status](https://travis-ci.org/pagehelper/Mybatis-PageHelper.svg?branch=master)](https://travis-ci.org/pagehelper/Mybatis-PageHelper)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/com.github.pagehelper/pagehelper/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.pagehelper/pagehelper)
[![Dependency Status](https://www.versioneye.com/user/projects/5932123f22f278003c5f851e/badge.svg?style=flat)](https://www.versioneye.com/user/projects/5932123f22f278003c5f851e)

[English](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/README.md)

如果你也在用 MyBatis，建议尝试该分页插件，这一定是<b>最方便</b>使用的分页插件。

分页插件支持任何复杂的单表、多表分页，部分特殊情况请看[重要提示](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/Important.md)。

想要使用分页插件？请看[如何使用分页插件](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/HowToUse.md)。

## 新增 JavaDoc 文档

文档地址：https://apidoc.gitee.com/free/Mybatis_PageHelper

Method API: https://apidoc.gitee.com/free/Mybatis_PageHelper/com/github/pagehelper/page/PageMethod.html


## 新书《MyBatis 从入门到精通》

![MyBatis 从入门到精通](https://github.com/mybatis-book/book/raw/master/book.png)

预售地址：[京东](https://item.jd.com/12103309.html)，[当当](http://product.dangdang.com/25098208.html)，[亚马逊](https://www.amazon.cn/MyBatis从入门到精通-刘增辉/dp/B072RC11DM/ref=sr_1_18?ie=UTF8&qid=1498007125&sr=8-18&keywords=mybatis)

CSDN博客：http://blog.csdn.net/isea533/article/details/73555400

GitHub项目：https://github.com/mybatis-book/book

## 支持 [MyBatis 3.1.0+](https://github.com/mybatis/mybatis-3)
## 物理分页

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
 12. `Derby`
 13. `Phoenix`
 
## 使用 [QueryInterceptor 规范](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/src/main/java/com/github/pagehelper/QueryInterceptor.java) 
[Executor 拦截器高级教程 - QueryInterceptor 规范](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/Interceptor.md)

## 分页插件 5.0

由于分页插件 5.0 版本和 4.2.x 实现完全不同，所以 master 分支为 5.x 版本，4.2 作为一个分支存在，如果有针对 4.2 的 PR，请注意提交到分支版本。

## 集成
使用 PageHelper 你只需要在 classpath 中包含 [pagehelper-x.x.x.jar](http://repo1.maven.org/maven2/com/github/pagehelper/pagehelper/) 和 [jsqlparser-0.9.5.jar](http://repo1.maven.org/maven2/com/github/jsqlparser/jsqlparser/0.9.5/)。

如果你使用 Maven，你只需要在 pom.xml 中添加下面的依赖：
```xml  
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper</artifactId>
    <version>最新版本</version>
</dependency>
```  

如果你使用 Spring Boot 可以参考： [pagehelper-spring-boot-starter](https://github.com/pagehelper/pagehelper-spring-boot)

[继续查看配置和用法](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/HowToUse.md)

## 文档：  

- [如何使用分页插件](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/HowToUse.md)
- [更新日志](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/Changelog.md)
- [重要提示](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/Important.md)

## Spring 集成示例

- [集成 Spring 3.x](https://github.com/abel533/Mybatis-Spring/tree/spring3.x)
- [集成 Spring 4.x](https://github.com/abel533/Mybatis-Spring)
- [集成 Spring Boot](https://github.com/abel533/MyBatis-Spring-Boot)

## 提交 BUG
- [提交到 github](https://github.com/pagehelper/Mybatis-PageHelper/issues/new)
- [提交到 gitosc](http://git.oschina.net/free/Mybatis_PageHelper/issues/new?issue%5Bassignee_id%5D=&issue%5Bmilestone_id%5D=)

## 作者信息

网站：http://www.mybatis.tk

作者博客：http://blog.csdn.net/isea533

作者邮箱： abel533@gmail.com  

如需加群，请通过 http://mybatis.tk 首页按钮加群。

本项目在 github 的项目地址：https://github.com/pagehelper/Mybatis-PageHelper

本项目在 gitosc 的项目地址：http://git.oschina.net/free/Mybatis_PageHelper

## MyBatis-3
- 项目：https://github.com/mybatis/mybatis-3
- 文档：http://mybatis.github.io/mybatis-3/zh/index.html  

MyBatis 专栏： 
- [MyBatis示例](http://blog.csdn.net/column/details/mybatis-sample.html)
- [MyBatis问题集](http://blog.csdn.net/column/details/mybatisqa.html)  
