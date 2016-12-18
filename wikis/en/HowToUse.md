##HOW TO USE  

###1. Installation

To use PageHelper you just need to include the 
[pagehelper-x.x.x.jar](http://repo1.maven.org/maven2/com/github/pagehelper/pagehelper/) 
and [jsqlparser-0.9.5.jar](http://repo1.maven.org/maven2/com/github/jsqlparser/jsqlparser/0.9.5/) file in the classpath.

If you are using Maven just add the following dependency to your pom.xml: 
```xml  
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper</artifactId>
    <version>x.x.x</version>
</dependency>
```

###2. Config PageHelper

####1. Using in mybatis-config.xml
```xml
<!-- 
    In the configuration file, 
    plugins location must meet the requirements as the following order:
    properties?, settings?, 
    typeAliases?, typeHandlers?, 
    objectFactory?,objectWrapperFactory?, 
    plugins?, 
    environments?, databaseIdProvider?, mappers?
-->
<plugins>
    <plugin interceptor="com.github.pagehelper.PageInterceptor">
        <!-- config params as the following -->
        <property name="param1" value="value1"/>
	</plugin>
</plugins>
```
####2. Using in Spring application.xml    
config `org.mybatis.spring.SqlSessionFactoryBean` as following:
```xml
<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
  <!-- other configuration -->
  <property name="plugins">
    <array>
      <bean class="com.github.pagehelper.PageInterceptor">
        <property name="properties">
          <!-- config params as the following -->
          <value>
            params=value1
          </value>
        </property>
      </bean>
    </array>
  </property>
</bean>
```   
####3. PageHelper Parameters  
PageHelper provides several optional parameters, 
these parameters when used in accordance with the above two examples to configuration.

Optional parameters as follows: 

- `dialect`: Default paging using PageHelper way, 
if you want to implement your own page logic, you can implement `Dialect`(`com.github.pagehelper.Dialect`) 
interface, and then configure the attribute to the fully qualified name of the implementing class.

**The following parameters are the parameters for the default dialect case. 
When implemented using a custom dialect, the following parameter has no effect.**

1. `helperDialect`: PageHelper automatically detects your current database links, 
automatically select the appropriate paging. 
You can also implement `AbstractHelperDialect`(`com.github.pagehelper.dialect.AbstractHelperDialect`), 
and then configure the attribute to achieve the fully qualified class name.

2. `offsetAsPageNum`: Default value is `false`，This parameter is valid for `RowBounds` as a pagination parameter.
When this parameter is set to `true`, the` offset` parameter in `RowBounds` is used as` pageNum`.

3. `rowBoundsWithCount`: Default value is `false`，When this parameter is set to `true`, 
PageHelper will execute count query(`PageRowBounds` not affected by this parameter).

4. `pageSizeZero`: Default value is `false`，When this parameter is set to `true`, 
if `pageSize=0` or `RowBounds.Limit = 0` will query all the results (the equivalent of a Paged query did not execute, 
but the return type of the result is still `Page`).


5. `reasonable`: Rationalization of paging parameters, Default value is `false`。
When this parameter is set to `true`,` pageNum <= 0` will query the first page,
`PageNum> pages` (over the total number), will query the last page. Default `false`, the query directly based on parameters.

6. `params`: In support of `startPage(Object params)` method，
The parameter is added to configure the parameter mapping for the value from the object based on the attribute name,
you can configure `pageNum,pageSize,count,pageSizeZero,reasonable`,
Default value is `pageNum=pageNum;pageSize=pageSize;count=countSql;reasonable=reasonable;pageSizeZero=pageSizeZero`。

7. `supportMethodsArguments`: Support via the Mapper interface parameters to pass the page parameter, the default value is 'false'.
The use of methods can refer to the test code in the `com.github.pagehelper.test.basic` package under the` ArgumentsMapTest` and `ArgumentsObjTest`.

8. `autoRuntimeDialect`: Default value is `false`。When set to `true`,
it is possible to automatically recognize pagination of the corresponding dialect at run time from multiple data sources
(Does not support automatic selection of `sqlserver2012`, can only use` sqlserver`), usage and precautions refer to the following **Scene 5**.


9. `closeConn`: Default value is **`true`**。
When you use a runtime dynamic data source or do not set the `helperDialect` property, PageHelper will automatically get the database type, then a database connection is automatically obtained,
This property is used to set whether to close the connection, the default `true` close. When 'false' is set, It will not close the connection.

####4. How to choose Configure these parameters

Here are a few examples for some of the parameters may be used.

#####Scene 1

If you are still in with a way to call a namespace like iBATIS, you might use `rowBoundsWithCount`.
If you want to count when the paging query query, you need to set this parameter to `true`.

In addition, another recommended way is to use the `PageRowBounds`, the use of this type as a paging parameter, the query will count the results into `PageRowBounds` in the `total` attribute.

#####Scene 2

If you are still in with a way to call a namespace like iBATIS, If  you think `RowBounds` in the two parameters` offset, limit` not as good as `pageNum, pageSize` easy to understand.
You can use the `offsetAsPageNum` parameter, when the parameter is set to `true`, `offset` as `pageNum`, `limit` and `pageSize` mean the same thing.

#####Scene 3
 
如果觉得某个地方使用分页后，你仍然想通过控制参数查询全部的结果，你可以配置 `pageSizeZero` 为 `true`，
配置后，当 `pageSize=0` 或者 `RowBounds.limit = 0` 就会查询出全部的结果。

#####Scene 4

如果你分页插件使用于类似分页查看列表式的数据，如新闻列表，软件列表，
你希望用户输入的页数不在合法范围（第一页到最后一页之外）时能够正确的响应到正确的结果页面，
那么你可以配置 `reasonable` 为 `true`，这时如果 `pageNum<=0` 会查询第一页，如果 `pageNum>总页数` 会查询最后一页。

#####Scene 5

如果你在 Spring 中配置了动态数据源，并且连接不同类型的数据库，这时你可以配置 `autoRuntimeDialect` 为 `true`，这样在使用不同数据源时，会使用匹配的分页进行查询。
这种情况下，你还需要特别注意 `closeConn` 参数，由于获取数据源类型会获取一个数据库连接，所以需要通过这个参数来控制获取连接后，是否关闭该连接。
默认为 `true`，有些数据库连接关闭后就没法进行后续的数据库操作。而有些数据库连接不关闭就会很快由于连接数用完而导致数据库无响应。所以在使用该功能时，特别需要注意你使用的数据源是否需要关闭数据库连接。

当不使用动态数据源而只是自动获取 `helperDialect` 时，数据库连接只会获取一次，所以不需要担心占用的这一个连接是否会导致数据库出错，但是最好也根据数据源的特性选择是否关闭连接。

###3. 如何在代码中使用  

阅读前请注意看[重要提示](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/Important.md)

首先分页插件支持以下两种调用方式:   

```java
//第一种，RowBounds方式的调用
List<Country> list = sqlSession.selectList("x.y.selectIf", null, new RowBounds(0, 10));

//第二种，Mapper接口方式的调用，推荐这种使用方式。
PageHelper.startPage(1, 10);
List<Country> list = countryMapper.selectIf(1);
```  

下面分别对这两种方式进行详细介绍

####1). RowBounds方式的调用   

```java
List<Country> list = sqlSession.selectList("x.y.selectIf", null, new RowBounds(1, 10));
```  
使用这种调用方式时，你可以使用RowBounds参数进行分页，这种方式侵入性最小，我们可以看到，通过RowBounds方式调用只是使用了这个参数，并没有增加其他任何内容。  

分页插件检测到使用了RowBounds参数时，就会对该查询进行<b>物理分页</b>。

关于这种方式的调用，有两个特殊的参数是针对 `RowBounds` 的，你可以参看上面的 **场景一** 和 **场景二**

<b>注: </b>不只有命名空间方式可以用RowBounds，使用接口的时候也可以增加RowBounds参数，例如:   

```java
//这种情况下也会进行物理分页查询
List<Country> selectAll(RowBounds rowBounds);  
```

**注意: ** 由于默认情况下的 `RowBounds` 无法获取查询总数，分页插件提供了一个继承自 `RowBounds` 的 `PageRowBounds`，这个对象中增加了 `total` 属性，执行分页查询后，可以从该属性得到查询总数。


####2). `PageHelper.startPage` 静态方法调用
除了 `PageHelper.startPage` 方法外，还提供了类似用法的 `PageHelper.offsetPage` 方法。

在你需要进行分页的 MyBatis 查询方法前调用 `PageHelper.startPage` 静态方法即可，紧跟在这个方法后的第一个**MyBatis 查询方法**会被进行分页。  

#####例一:   

```java
//获取第1页，10条内容，默认查询总数count
PageHelper.startPage(1, 10);
//紧跟着的第一个select方法会被分页
List<Country> list = countryMapper.selectIf(1);
assertEquals(2, list.get(0).getId());
assertEquals(10, list.size());
//分页时，实际返回的结果list类型是Page<E>，如果想取出分页信息，需要强制转换为Page<E>
assertEquals(182, ((Page) list).getTotal());
```

#####例二: 
```java
//获取第1页，10条内容，默认查询总数count
PageHelper.startPage(1, 10);
//紧跟着的第一个select方法会被分页
List<Country> list = countryMapper.selectIf(1);

//后面的不会被分页，除非再次调用PageHelper.startPage
List<Country> list2 = countryMapper.selectIf(null);
//list1
assertEquals(2, list.get(0).getId());
assertEquals(10, list.size());
//分页时，实际返回的结果list类型是Page<E>，如果想取出分页信息，需要强制转换为Page<E>，
//或者使用PageInfo类（下面的例子有介绍）
assertEquals(182, ((Page) list).getTotal());
//list2
assertEquals(1, list2.get(0).getId());
assertEquals(182, list2.size());
```  

#####例三，使用`PageInfo`的用法:   

```java
//获取第1页，10条内容，默认查询总数count
PageHelper.startPage(1, 10);
List<Country> list = countryMapper.selectAll();
//用PageInfo对结果进行包装
PageInfo page = new PageInfo(list);
//测试PageInfo全部属性
//PageInfo包含了非常全面的分页属性
assertEquals(1, page.getPageNum());
assertEquals(10, page.getPageSize());
assertEquals(1, page.getStartRow());
assertEquals(10, page.getEndRow());
assertEquals(183, page.getTotal());
assertEquals(19, page.getPages());
assertEquals(1, page.getFirstPage());
assertEquals(8, page.getLastPage());
assertEquals(true, page.isFirstPage());
assertEquals(false, page.isLastPage());
assertEquals(false, page.isHasPreviousPage());
assertEquals(true, page.isHasNextPage());
```

除了这几种常见的用法外，在下面的安全调用中，也有一些特殊的用法。

####3). `PageHelper` 安全调用

#####1.使用 `RowBounds` 和 `PageRowBounds` 参数方式是极其安全的。

#####2.使用参数方式是极其安全的
想要使用参数方式，需要配置 `supportMethodsArguments` 参数为 `true`，同时要配置 `params` 参数。
例如下面的配置: 
```xml
<plugins>
    <!-- com.github.pagehelper为PageHelper类所在包名 -->
    <plugin interceptor="com.github.pagehelper.PageInterceptor">
        <!-- 使用下面的方式配置参数，后面会有所有的参数介绍 -->
        <property name="supportMethodsArguments" value="true"/>
        <property name="params" value="pageNum=pageNumKey;pageSize=pageSizeKey;"/>
	</plugin>
</plugins>
```
在 MyBatis 方法中: 
```java
List<Country> selectByPageNumSizeOrderBy(
        @Param("user") User user,
        @Param("pageNumKey") int pageNum, 
        @Param("pageSizeKey") int pageSize);
```
当调用这个方法时，由于同时发现了 `pageNumKey` 和 `pageSizeKey` 参数，这个方法就会被分页。params 提供的几个参数都可以这样使用。

除了上面这种方式外，如果 User 对象中包含这两个参数值，也可以有下面的方法: 
```java
List<Country> selectByPageNumSizeOrderBy(User user);
```
当从 User 中同时发现了 `pageNumKey` 和 `pageSizeKey` 参数，这个方法就会被分页。

注意: `pageNum` 和 `pageSize` 两个属性同时存在才会触发分页操作，在这个前提下，其他的分页参数才会生效。

#####3.手动调用 `PageHelper.clearPage()`
`PageHelper` 方法使用了静态的 `ThreadLocal` 参数，分页参数和线程是绑定的。

只要你可以保证在 `PageHelper` 方法调用后紧跟 MyBatis 查询方法，这就是安全的。因为 `PageHelper` 在 `finally` 代码段中自动清除了 `ThreadLocal` 存储的对象。

如果代码在进入 `Executor` 前发生异常，就会导致线程不可用，这属于人为的 Bug，而不是运行时的问题，这种情况由于线程不可用，也不会导致 `ThreadLocal` 参数被错误的使用。

但是如果你写出下面这样的代码，就是不安全的用法: 
```java
PageHelper.startPage(1, 10);
List<Country> list;
if(param1 != null){
    list = countryMapper.selectIf(param1);
} else {
    list = new ArrayList<Country>();
}
```
这种情况下由于 param1 存在 null 的情况，就会导致 PageHelper 生产了一个分页参数，但是没有被消费，这个参数就会一直保留在这个线程上。当这个线程再次被使用时，就可能导致不该分页的方法去消费这个分页参数，这就产生了莫名其妙的分页。

上面这个代码，应该写成下面这个样子: 
```java
List<Country> list;
if(param1 != null){
    PageHelper.startPage(1, 10);
    list = countryMapper.selectIf(param1);
} else {
    list = new ArrayList<Country>();
}
```
这种写法就能保证安全。

如果你对此不放心，你可以手动清理 `ThreadLocal` 存储的分页参数，可以像下面这样使用: 
```java
List<Country> list;
if(param1 != null){
    PageHelper.startPage(1, 10);
    try{
        list = countryMapper.selectAll();
    } finally {
        PageHelper.clearPage();
    }
} else {
    list = new ArrayList<Country>();
}
```
这么写很不好看，而且没有必要。

#####4. 使用 ISelect 接口调用

用法如下: 
```java
//jdk6,7用法，创建接口
Page<Country> page = PageHelper.startPage(1, 10).doSelectPage(new ISelect() {
    @Override
    public void doSelect() {
        countryMapper.selectGroupBy();
    }
});
//jdk8 lambda用法
Page<Country> page = PageHelper.startPage(1, 10).doSelectPage(()-> countryMapper.selectGroupBy());
//为了说明可以链式使用，上面是单独setOrderBy("id desc")，也可以直接如下
Page<Country> page = PageHelper.startPage(1, 10).doSelectPage(()-> countryMapper.selectGroupBy());

//也可以直接返回PageInfo，注意doSelectPageInfo方法和doSelectPage
pageInfo = PageHelper.startPage(1, 10).doSelectPageInfo(new ISelect() {
    @Override
    public void doSelect() {
        countryMapper.selectGroupBy();
    }
});
//对应的lambda用法
pageInfo = PageHelper.startPage(1, 10).doSelectPageInfo(() -> countryMapper.selectGroupBy());

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
除了可以保证安全外，还特别实现了将查询转换为单纯的 count 查询方式，这个方法可以将任意的查询方法，变成一个 `select count(*)` 的查询方法。

使用接口匿名类编程时，查询需要参数需要设置为 final，这样也不是很方便。

###4. MyBatis 和 Spring 集成示例

如果和Spring集成不熟悉，可以参考下面两个

<b>只有基础的配置信息，没有任何现成的功能，作为新手入门搭建框架的基础</b>

- [集成 Spring 3.x](https://github.com/abel533/Mybatis-Spring/tree/spring3.x)
- [集成 Spring 4.x](https://github.com/abel533/Mybatis-Spring)

这两个集成框架集成了 PageHelper 和 [通用 Mapper](https://github.com/abel533/Mapper)。

###5. Spring Boot 待定