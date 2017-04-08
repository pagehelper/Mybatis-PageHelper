## 更新日志

### 5.0.1-beta - 2017-04-08

- 增加新的参数 `countColumn` 用于配置自动 count 查询时的查询列，默认值`0`，也就是 `count(0)`
- `Page` 对象也新增了 `countColumn` 参数，可以针对具体查询进行配置
- 针对文档显示问题进行修改，by liumian* [PR #30](https://github.com/pagehelper/Mybatis-PageHelper/pull/30)

### 5.0.0 - 2017-01-02

- 使用 [QueryInterceptor 规范](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/src/main/java/com/github/pagehelper/QueryInterceptor.java) 处理分页逻辑
- 新的分页插件拦截器为 `com.github.pagehelper.PageInterceptor`
- 新的 `PageHelper` 是一个特殊的 `Dialect` 实现类，以更友好的方式实现了以前的功能
- 新的分页插件仅有 `dialect` 一个参数，默认的 `dialect` 实现类为 `PageHelper` 
- `PageHelper` 仍然支持以前提供的参数，在最新的使用文档中已经全部更新
- `PageHelper` 的 `helperDialect` 参数和以前的 `dialect` 功能一样，具体可以看文档的参数说明
- 增加了基于纯 `RowBounds` 和 `PageRowBounds` 的分页实现，在 `com.github.pagehelper.dialect.rowbounds` 包中，这是用于作为 `dialect` 参数示例的实现，后面会补充更详细的文档
- 去掉了不适合出现在分页插件中的 orderby 功能，以后会提供单独的排序插件
- 去掉了 `PageHelper` 中不常用的方法
- 新的文档，更新历来更新日志中提到的重要内容，提供英文版本文档
- 解决 bug [#149](http://git.oschina.net/free/Mybatis_PageHelper/issues/149)
- 将 Db2RowDialect 改为 Db2RowBoundsDialect
- 所有分页插件抛出的异常改为 PageException

### 4.2.1 - 2016-12-11

- 解决`SimpleCache`类遗留问题导致的错误 [#143](http://git.oschina.net/free/Mybatis_PageHelper/issues/143) fix by [dhhua](https://github.com/dhhua)

### 4.2.0 - 2016-12-09

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

### 4.1.6 - 2016-06-05

- 通过间接处理字符串解决SqlServer中不支持`with(nolock)`的问题#86，详情可以看`SqlServerParser`和`SqlServer2012Dialect`

### 4.1.5 - 2016-05-29

- 更新`PageProviderSqlSource`，支持3.4.0版本的`Provider`注解方式的分页#102
- 解决`SqlUtil`未初始化`PARAMS`属性导致的错误

### 4.1.4 - 2016-05-12

- 解决`closeConn`未设置时，默认值被覆盖变成`false`的问题#97
- `closeConn`不只对动态数据源有效，当没有设置`dialect`属性自动获取数据库类型的时候同样有效
- 解决关闭tomcat的时候提示线程安全问题#98，这个问题不会导致内存溢出，已经增加处理

### 4.1.3 - 2016-03-31

- 解决反射类没有完全捕获异常的问题#94
- 把SqlServer类所有private都改成了protected，方便继承修改#93

### 4.1.2 - 2016-03-06

- 增加可配参数`closeConn`，当使用动态数据源时，分页插件获取jdbcUrl后，控制是否关闭当前连接，默认`true`关闭
- count查询改为`count(0)`，分库分表情况下的效率可能更高

### 4.1.1 - 2016-01-05：

- 解决动态数据源时获取连接后未关闭的严重bug#80
- 解决动态数据源时SqlSource和parser绑定导致不能切换方言的问题

### 4.1.0 - 2015-12-30：

- 增加`autoRuntimeDialect`参数，允许在运行时根据多数据源自动识别对应方言的分页（暂时不支持自动选择`sqlserver2012`，只能使用`sqlserver`）。
- 去掉了4.0.3版本增加的`returnPageInfo`参数，接口返回值不支持`PageInfo`类型，可以使用下面`ISelect`中演示的方法获取
- 增加对`SqlServer2012`的支持，需要手动指定`dialect=sqlserver2012`，否则会使用2005的方式进行分页
- jsqlparser升级到0.9.4版本，使用jar包时必须用最新的0.9.4版本，使用Maven会自动依赖0.9.4
- 增加`ISelect`接口，方便调用，使用方法可以参考`src/test/java/com.github.pagehelper.test.basic.TestISelect`测试。

### 使用该接口可以参考如下用法(返回值为`Page`或`PageInfo`)：

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

### 4.0.3 - 2015-11-09：

 - `PageHelper`新增3个`offsetPage`方法，参数主要是`offset`和`limit`，允许不规则分页

 - 新增两个可配参数`supportMethodsArguments`和`returnPageInfo`（该参数在4.1.0版本去掉），具体含义和用法请看[如何使用分页插件](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/HowToUse.md)中的参数介绍

### 4.0.2 - 2015-11-02

 - 简化`Page<E>`类，包含排序条件`orderBy`

 - `dialect`参数是数据库名称时不区分大小写

 - `dialect`参数可以设置为实现`com.github.pagehelper.parser.Parser`接口的实现类全限定名称

 - 增加对`h2`数据库的支持

 - 将`OrderByHelper`(排序插件)融合到`PageHelper`中，移除`OrderByHelper`

 - 该版本调整比较大，但对开发人员影响较小，为以后扩展和完善提供方便

### 4.0.1 -2015-09-10

 - 解决[#60 -使用RPC时，因Page类引用了RowBounds，导致反序列化失败](http://git.oschina.net/free/Mybatis_PageHelper/issues/60) by [马金凯](http://git.oschina.net/mxb)

 - 这个改动主要是去掉了`Page<E>`构造方法中的`RowBounds`，用`int[]`数组替换了`RowBounds`

### 4.0.0 - 2015-07-13

 - 配置属性`dialect`不在强制要求，可以不写，分页插件会自动判断

 - 解决从request中获取分页参数时的错误,感谢<b>探路者☆</b>

 - `PageInfo`增加空构造方法，所有属性增加`setter`方法

 - 增加对排序的支持

 - 可以单独使用`PageHelper.orderBy(String orderBy)`对查询语句增加排序，也可以配合`startPage`的其他方法使用

 - 可以使用`PageHelper.startPage(int start,int size,String orderBy)`对分页查询进行排序

 - 修改分页查询的处理逻辑，主要是将原`sqlSource`包装成可以分页和排序的`sqlSource`

### 3.7.5 - 2015-06-12

 - 增加对MyBatis3.2.0以上版本的校验，如果是不是3.2.0以上版本，会抛出异常提示

 - 解决3.7.1更新中实际没有解决的入参为不可变`Map`类型时的错误

### 3.7.4 - 2015-05-26

 - 为了支持`3.3.0`去掉了分页插件自带的`SytemObjectMetaObject`类(该类在早期版本为了支持3.2.0以前的MyBatis)

 - 最新支持MyBatis - 3.2.0到最新3.3.0版本

### 3.7.3 - 2015-05-22

 - `Page`继承的`ArrayList`，会根据`pageSize`初始化大小，这就导致当`pageSize`过大（如`Integer.MAX_VALUE`），实际数据量很小时的内存溢出，此处改为初始化大小为0的`List`。

 - 当想查询某页后面的全部数据时，可以使用`PageHelper.startPage(pageNum, Integer.MAX_VALUE)`进行分页，`RowBounds(offset, Integer.MAX_VALUE)`一样。

 - 针对`PageHelper.startPage(1, Integer.MAX_VALUE)`优化，会取消分页，直接查询全部数据（能起到`pageSizeZero`参数所起的作用）。

 - 针对`RowBounds(0, Integer.MAX_VALUE)`优化，会取消分页，直接查询全部数据（能起到`pageSizeZero`参数所起的作用）。

### 3.7.2 - 2015-05-13

 - jsqlparser解析sql会抛出Error异常，由于只捕获Exception，所以导致部分解析失败的sql无法使用嵌套方式处理，所以修改为捕获`Throwable`。

### 3.7.1 - 2015-05-05

 - 增加`Infomix`数据库支持，设置`dialect`值为`infomix`即可
 - 解决入参为不可变`Map`类型时的错误

### 3.7.0 - 2015-04-21

 - 由于`orderby`参数经常被错误认为的使用，因此该版本全面移除了`orderby`
 - `Page<E>`移除`orderby`属性
 - `PageHelper`的`startPage`方法中，移除包含`orderby`参数的方法，sqlserver相关包含该参数的全部移除
 - 对SqlServer进行分页查询时，请在sql中包含order by语句，否则会抛出异常
 - 当`offsetAsPageNum=false`的时候，由于PageNum问题，`RowBounds`查询的时候`reasonable`会强制为false，已解决
 - 少数情况下的select中包含单个函数查询时，会使用嵌套的count查询

### 3.6.4 - 2015-04-05

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

### 3.6.3 - 2015-03-10

 - 解决了一个潜在的bug，对[通用Mapper](http://git.oschina.net/free/Mapper)中的`SqlMapper`进行分页时，需要使用这个版本

### 3.6.2 - 2015-03-09

 - 本次更新只是增加了一个异常提示，当错误的配置了多个分页插件时，会有更友好的错误提示：
 
   >分页插件配置错误:请不要在系统中配置多个分页插件(使用Spring时,mybatis-config.xml和Spring<bean>配置方式，请选择其中一种，不要同时配置多个分页插件)！

### 3.6.1 - 2015-02-28

 - 解决select distinct导致count查询结果不正确的bug#35
 
 - 完善测试

### 3.6.0 - 2015-02-03

 - 支持db2数据库
 
 - 支持sqlserver(2005+)数据库
 
 - sqlserver注意事项： 
   - 请先保证你的SQL可以执行
   - sql中最好直接包含order by，可以自动从sql提取
   - 如果没有order by，可以通过入参提供，但是需要自己保证正确(<b>3.7.0版本以后，移除了该参数，请在sql中包含order by</b>)
   - 如果sql有order by，可以通过orderby参数覆盖sql中的order by
   - order by的列名不能使用别名(`UNION,INTERSECT,MINUS,EXCEPT`等复杂sql不受限制，具体可以自己尝试)
   - 表和列使用别名的时候不要使用单引号(')

 - 简单修改结构
 
 - `startPage`方法返回值从`void`改为`Page`，获取`Page`后可以修改参数值
 
 - `Page`增加一个针对sqlserver的属性`orderBy`(<b>3.7.0版本以后，移除了该属性</b>)，用法看上面的<b>注意事项</b>
 
 - `Page`增加了一个链式赋值的方法，可以像下面这样使用：
   `PageHelper.startPage(1,10).count(false).reasonable(true).pageSizeZero(false)`
   
 - `PageHelper`增加了`startPage(int pageNum, int pageSize,String orderBy)`方法(<b>3.7.0版本以后，移除了该方法</b>)，针对sqlserver

### 3.5.1 - 2015-01-20

 - 解决[bug#25](http://git.oschina.net/free/Mybatis_PageHelper/issues/25)，当参数是null并且是动态查询时，由于加入分页参数，导致参数不在是null，因而会导致部分判断出错，导致异常。
 
 - 上面这个bug会影响使用了动态标签并且允许入参为null的所有查询，虽然并不常见，但是建议各位使用最新版本

### 3.5.0 - 2015-01-11

 - 增加更丰富的调用方法[#23](http://git.oschina.net/free/Mybatis_PageHelper/issues/23)
   - `startPage(int pageNum, int pageSize)`
   - `startPage(int pageNum, int pageSize, boolean count)`
   - +`startPage(int pageNum, int pageSize, boolean count, Boolean reasonable)`
   - +`startPage(int pageNum, int pageSize, boolean count, Boolean reasonable, Boolean pageSizeZero)`
   - +`startPage(Object params)`<b>注：只能是`Map`或`ServletRequest`类型</b>

   参数中的`reasonable`、`pageSizeZero`都可以覆盖默认配置，如果传`null`会用默认配置。

 - 为了支持`startPage(Object params)`方法，增加了一个`params`参数来配置参数映射，用于从`Map`或`ServletRequest`中取值，详细内容看文档下面的具体介绍。

 - 解决一个`<foreach>`标签使用对象内部属性循环时的bug[#24](http://git.oschina.net/free/Mybatis_PageHelper/issues/24)

### 3.4.2 - 2014-12-27

- `PageInfo`中的`judgePageBoudary`方法修改：
   ```java
    isLastPage = pageNum == pages && pageNum != 1;
    //改为
    isLastPage = pageNum == pages;
   ```

### 3.4.1 - 2014-12-24

 - 重大bug修复，`SqlParser`解析sql失败的时候返回了不带`count(*)`的sql，导致查询失败。
 
 - 产生原因，由于`SqlParser`在系统中出现的位置修改后，导致这里出现错误。
 
 - 强烈推荐各位更新到最新版本。
 
### v3.4.0 - 2014-12-18

 - 增加了对`@SelectProvider`注解方法的支持，不使用这种方式的不影响
 
 - 对基本逻辑进行修改，减少反射调用和获取`BoundSql`次数
 
### v3.3.2 - 2014-12-10

 - `PageInfo` 增加序列化。
 
### v3.3.1bug修复 - 2014-12-07

- 动态sql时，判断条件不会出现在ParameterMappings中，会导致获取不到属性。通常是因为判断条件中的属性没有出现在`#{}`中。

### v3.3.0

1. 对`MappedStatement`对象进行缓存，包括count查询的`MappedStatement`以及分页查询的`MappedStatement`，分页查询改为预编译查询。

2. 独立的`SqlUtil`类，由于原来的`PageHelper`太复杂，因此将拦截器外的其他代码独立到`SqlUtil`中，方便查看代码和维护。`SqlUtil`中增加`Parser`接口，提供一个抽象的`SimpleParser`实现，不同数据库的分页代码通过继承`SimpleParser`实现。

3. 特殊的`Parser`实现类`SqlParser`类，这是一个独立的java类，主要提供了更高性能的count查询sql，可以根据sql自动改为`count(*)`查询，自动去除不需要的`order by`语句，如果需要使用该类，只要把该类放到`SqlUtil`类相同的包下即可，同时需要引入Jar包`jsqlparser-0.9.1.jar`。

4. 增强的`PageInfo`类，`PageInfo`类包含了分页几乎所有需要用到的属性值，减少了对分页逻辑的过多投入。  

4. 分页合理化，自动处理pageNum的异常情况。例如当pageNum<=0时，会设置pageNum=1，然后查询第一页。当pageNum>pages(总页数)时，自动将pageNum=pages，查询最后一页。  

5. 增加对`PostgreSQL`,`MariaDB`,`SQLite`支持。其中`MariaDB`,`SQLite`和`Mysql`分页一样。

### v3.2.3  

1. 解决`mysql`带有`for update`时分页错误的问题。  

2. 当`pageSize`（或`RowBounds`的`limit`）`<=0` 时不再进行分页查询，只会进行count查询（RowBounds需要配置进行count查询），相当于用分页查询来做count查询了。  

3. 增加了`pageSizeZero`参数，当`pageSizeZero=true`时，如果`pageSize=0`（或`RowBounds.limit`=0），就会查询全部的结果。这个参数对于那些在特殊情况下要查询全部结果的人有用。配置该参数后会与上面第二条冲突，解决方法就是如果只想查询count，就设置`pageSize<0`（如 `-1`），只要不等于0（或者不配置pageSizeZero）就不会出现全部查询的情况。  

4. 这个版本没有包含count查询时自动去除`order by`的功能，这个功能将会添加到3.3.0版本中。  

5. 为了便于本项目的统一管理和发布，本项目会和github上面同步，项目会改为Maven管理的结构。  


### v3.2.2

1. 简单重构优化代码。  

2. 新增`PageInfo`包装类，对分页结果Page<E>进行封装，方便EL使用。

3. 将`SystemMetaObject`类的`fromObject`方法内置到分页插件中，方便低版本的Mybatis使用该插件。   

### v3.2.1

1. 新增`offsetAsPageNum`参数，用来控制`RowBounds`中的`offset`是否作为`pageNum`使用，`pageNum`和`startPage`中的含义相同，`pageNum`是页码。该参数默认为`false`，使用默认值时，不需要配置该参数。

2. 新增`rowBoundsWithCount`参数，用来控制使用`RowBounds`时是否执行`count`查询。该参数默认为`false`，使用默认值时，不需要配置该参数。

### v3.2.0

1. 增加了对`Hsqldb`的支持，主要目的是为了方便测试使用`Hsqldb`  

2. 增加了该项目的一个测试项目[Mybatis-Sample](http://git.oschina.net/free/Mybatis-Sample)，测试项目数据库使用`Hsqldb`  

3. 增加MIT协议

### v3.1.2

1. 解决count sql在`oracle`中的错误

### v3.1.1 
 
1. 统一返回值为`Page<E>`（可以直接按`List`使用）,方便在页面使用EL表达式，如`${page.pageNum}`,`${page.total}`     
   
### v3.1.0
  
1. 解决了`RowBounds`分页的严重BUG，原先会在物理分页基础上进行内存分页导致严重错误，已修复  

2. 增加对MySql的支持，该支持由[鲁家宁](http://my.oschina.net/lujianing)增加。  
  
### v3.0 
 
1. 现在支持两种形式的分页，使用`PageHelper.startPage`方法或者使用`RowBounds`参数   

2. `PageHelper.startPage`方法修改，原先的`startPage(int pageNum, int pageSize)`默认求count，新增的`startPage(int pageNum, int pageSize, boolean count)`设置`count=false`可以不执行count查询  

3. 移除`endPage`方法，现在本地变量`localPage`改为取出后清空本地变量。  

4. 修改`Page<E>`类，继承`ArrayList<E>`  

5. 关于两种形式的调用，请看示例代码   
    
### v2.1    

1. 解决并发异常  

2. 分页sql改为直接拼sql    

### v2.0  

1. 支持Mybatis缓存，count和分页同时支持（二者同步）  

2. 修改拦截器签名，拦截`Executor`

3. 将`Page<E>`类移到外面，方便调用

### v1.0  

1. 支持`<foreach>`等标签的分页查询  

2. 提供便捷的使用方式  

