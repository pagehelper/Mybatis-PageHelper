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

##分页插件最新版本为4.0.3

###Maven坐标

```xml  
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper</artifactId>
    <version>4.0.3</version>
</dependency>
```  

###下载JAR包

分页插件pagehelper.jar： 

 - https://oss.sonatype.org/content/repositories/releases/com/github/pagehelper/pagehelper/
 
 - http://repo1.maven.org/maven2/com/github/pagehelper/pagehelper/

由于使用了sql解析工具，你还需要下载jsqlparser.jar（这个文件完全独立，不依赖其他）：  

 - http://repo1.maven.org/maven2/com/github/jsqlparser/jsqlparser/0.9.4/
 
 - http://git.oschina.net/free/Mybatis_PageHelper/attach_files

##4.1.0更新日志：

- 增加`autoRuntimeDialect`参数，允许在运行时根据多数据源自动识别对应方言的分页，<b>该参数会先在快照版中进行测试，如果没有收到任何反馈，正式版会取消该参数<b>
- 增加对`SqlServer2012`的支持，需要手动指定`dialect=sqlserver2012`，否则会使用2005的方式进行分页
- jsqlparser升级到0.9.4版本，使用jar包时必须用最新的0.9.4版本，使用Maven会自动依赖0.9.4
- 增加`ISelect`接口，方便调用，使用方法可以参考`src/test/java/com.github.pagehelper.test.basic.TestISelect`测试。
  使用该接口可以参考如下用法(返回值为`Page`或`PageInfo`)：
```java
//jdk6,7用法，创建接口
Page<Country> page = PageHelper.startPage(1, 10).setOrderBy("id desc").doSelectPage(new ISelect() {
    @Override
    public void doSelect() {
        countryMapper.selectGroupBy();
    }
});
//jdk8 lambda用法
Page<Country> page = PageHelper.startPage(1, 10).setOrderBy("id desc").doSelectPage(()-> countryMapper.selectGroupBy());
//为了说明可以链式使用，上面是单独setOrderBy("id desc")，也可以直接如下
Page<Country> page = PageHelper.startPage(1, 10, "id desc").doSelectPage(()-> countryMapper.selectGroupBy());

//也可以直接返回PageInfo，注意doSelectPageInfo方法和doSelectPage
pageInfo = PageHelper.startPage(1, 10).setOrderBy("id desc").doSelectPageInfo(new ISelect() {
    @Override
    public void doSelect() {
        countryMapper.selectGroupBy();
    }
});
//对应的lambda用法
pageInfo = PageHelper.startPage(1, 10).setOrderBy("id desc").doSelectPageInfo(() -> countryMapper.selectGroupBy());

//count查询，返回一个查询语句的count数
long total = PageHelper.count(new ISelect() {
    @Override
    public void doSelect() {
        countryMapper.selectLike(country);
    }
});
//lambda
total = PageHelper.count(()->countryMapper.selectLike(country));
```
- 实际上大家可以自己包装一下`PageHelper`类，增加定制化方法

##4.0.3更新日志：

 - `PageHelper`新增3个`offsetPage`方法，参数主要是`offset`和`limit`，允许不规则分页

 - 新增两个可配参数`supportMethodsArguments`和`returnPageInfo`，具体含义和用法请看[如何使用分页插件](http://git.oschina.net/free/Mybatis_PageHelper/blob/master/wikis/HowToUse.markdown)中的参数介绍

##4.0.2更新日志：

 - 简化`Page<E>`类，包含排序条件`orderBy`

 - `dialect`参数是数据库名称时不区分大小写

 - `dialect`参数可以设置为实现`com.github.pagehelper.parser.Parser`接口的实现类全限定名称

 - 增加对`H2`数据库的支持

 - 将`OrderByHelper`(排序插件)融合到`PageHelper`中，移除`OrderByHelper`

 - 该版本调整比较大，但对开发人员影响较小，为以后扩展和完善提供方便

##4.0.1更新日志：

 - 解决[#60 -使用RPC时，因Page类引用了RowBounds，导致反序列化失败](http://git.oschina.net/free/Mybatis_PageHelper/issues/60) by [马金凯](http://git.oschina.net/mxb)

 - 这个改动主要是去掉了`Page<E>`构造方法中的`RowBounds`，用`int[]`数组替换了`RowBounds`

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

作者邮箱： abel533@gmail.com  

Mybatis工具群： <a target="_blank" href="http://shang.qq.com/wpa/qunwpa?idkey=29e4cce8ac3c65d14a1dc40c9ba5c8e71304f143f3ad759ac0b05146e0952044"><img border="0" src="http://pub.idqqimg.com/wpa/images/group.png" alt="Mybatis工具" title="Mybatis工具"></a>