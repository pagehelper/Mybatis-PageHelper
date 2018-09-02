## HOW TO USE  

### 1. Installation

To use PageHelper you just need to include the 
[pagehelper-x.y.z.jar](http://repo1.maven.org/maven2/com/github/pagehelper/pagehelper/)
and [jsqlparser-x.y.z.jar](http://repo1.maven.org/maven2/com/github/jsqlparser/jsqlparser/) file in the classpath.

If you are using Maven just add the following dependency to your pom.xml: 
```xml  
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper</artifactId>
    <version>x.x.x</version>
</dependency>
```

### 2. Config PageHelper

#### 1. Using in mybatis-config.xml
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
#### 2. Using in Spring application.xml    
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
            param1=value1
          </value>
        </property>
      </bean>
    </array>
  </property>
</bean>
```   
#### 3. PageHelper Parameters  
PageHelper provides several optional parameters, 
these parameters when used in accordance with the above two examples to configuration.

Optional parameters as follows: 

- `dialect`: Default paging using PageHelper way, 
if you want to implement your own page logic, you can implement `Dialect`(`com.github.pagehelper.Dialect`) 
interface, and then configure the attribute to the fully qualified name of the implementing class.

**The following parameters are the parameters for the default dialect case. 
When implemented using a custom dialect, the following parameter has no effect.**

1. `helperDialect`: PageHelper will detect the current database url by default, automatically select the corresponding database dialect.
You can configure `helperDialect` Property to specify the dialect. You can use the following abbreviations :  
`oracle`, `mysql`, `mariadb`, `sqlite`, `hsqldb`, `postgresql`,
`db2`, `sqlserver`, `informix`, `h2`, `sqlserver2012`, `derby`.  
You can also implement `AbstractHelperDialect`, 
and then configure the attribute to achieve the fully qualified class name.  
**Special note :** When using the SqlServer2012 database,
you need to manually specify for `sqlserver2012`, otherwise it will use the SqlServer2005 for paging.

2. `offsetAsPageNum`: Default value is `false`, This parameter is valid for `RowBounds` as a pagination parameter.
When this parameter is set to `true`, the` offset` parameter in `RowBounds` is used as` pageNum`.

3. `rowBoundsWithCount`: Default value is `false`, When this parameter is set to `true`, 
PageHelper will execute count query.

4. `pageSizeZero`: Default value is `false`, When this parameter is set to `true`, 
if `pageSize=0` or `RowBounds.Limit = 0` will query all the results (the equivalent of a Paged query did not execute, 
but the return type of the result is still `Page`).


5. `reasonable`: Rationalization of paging parameters, Default value is `false`。
When this parameter is set to `true`,` pageNum <= 0` will query the first page,
`PageNum> pages` (over the total number), will query the last page. Default `false`, the query directly based on parameters.

6. `params`: In support of `startPage(Object params)` method, 
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

10. `aggregateFunctions`(5.1.5+): The default is the aggregate function of all common databases,
allowing you to manually add aggregate functions ( affecting the number of rows ).
All functions that start with aggregate functions will be wrap as subquery.
Other functions and columns will be replaced with count(0).

#### 4. How to choose Configure these parameters

Here are a few examples for some of the parameters may be used.

##### Scene 1

If you are still in with a way to call a namespace like iBATIS, you might use `rowBoundsWithCount`.
If you want to count when the paging query query, you need to set this parameter to `true`.

**Note:** `PageRowBounds` also need `true`.
##### Scene 2

If you are still in with a way to call a namespace like iBATIS, If  you think `RowBounds` in the two parameters` offset, limit` not as good as `pageNum, pageSize` easy to understand.
You can use the `offsetAsPageNum` parameter, when the parameter is set to `true`, `offset` as `pageNum`, `limit` and `pageSize` mean the same thing.

##### Scene 3

If you feel you have to paginate a page somewhere and you still want to query all the results with control parameters.
You can configure `pageSizeZero` to` true`,
After configuration, when `pageSize = 0` or `RowBounds.limit = 0` will query all the results.

##### Scene 4

If you want the user to enter the page number is not in the legal scope (the first page to the last page) to correctly respond to the correct results page,
Then you can configure `reasonable` to` true`, and if `pageNum <= 0` will query the first page, the `pageNum> pages(total pages)` will query the last page.

##### Scene 5

If you configure dynamic data sources in Spring and connect different types of databases, 
you can configure `autoRuntimeDialect` to` true`, 
which will use matching pagination queries when using different data sources.
In this case, you also need to pay attention to the `closeConn` parameter, 
because the type of access to the data source will get a database connection, 
so the need to control this parameter to obtain a connection, whether to close the connection.

Default is `true`, and some database connections can not be closed after the follow-up database operations. 
And some database connections will not be closed soon because the number of connections out of the database caused no response.
Therefore, when using this feature, in particular, you need to pay attention to whether the use of the data source needs to close the database connection.

When you do not use dynamic data sources but only automatically get `helperDialect`, 
the database connection will only get once, 
so there is no need to worry about whether this connection will lead to a database error, 
but also according to the characteristics of the data source to choose whether to close the connection.

### 3. How to use in your code

Please note before reading [Important Notice](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/Important.md)

PageHelper supports the following usage:  

```java
//1. use by RowBounds
List<Country> list = sqlSession.selectList("x.y.selectIf", null, new RowBounds(0, 10));
//or interface
List<Country> list = countryMapper.selectIf(1, new RowBounds(0, 10));
//or PageRowBounds
PageRowBounds rowBounds = new PageRowBounds(0, 10);
List<Country> list = countryMapper.selectIf(1, new RowBounds(0, 10));
long total = rowBounds.getTotal();

//2. use static method startPage
PageHelper.startPage(1, 10);
List<Country> list = countryMapper.selectIf(1);

//3. use static method offsetPage
PageHelper.offsetPage(1, 10);
List<Country> list = countryMapper.selectIf(1);

//4. method parameters
public interface CountryMapper {
    List<Country> selectByPageNumSize(
            @Param("user") User user,
            @Param("pageNum") int pageNum, 
            @Param("pageSize") int pageSize);
}
//config supportMethodsArguments=true
List<Country> list = countryMapper.selectByPageNumSize(user, 1, 10);

//5. POJO parameters
public class User {
    //other fields
    //The following two parameters must be the same name as the params parameter
    private Integer pageNum;
    private Integer pageSize;
}
public interface CountryMapper {
    List<Country> selectByPageNumSize(User user);
}
//When the pageNum! = null && pageSize! = null in the user instance, this method will be automatically pagination
List<Country> list = countryMapper.selectByPageNumSize(user);

//6. ISelect interface
//jdk6,7 anonymous class, return Page
Page<Country> page = PageHelper.startPage(1, 10).doSelectPage(new ISelect() {
    @Override
    public void doSelect() {
        countryMapper.selectGroupBy();
    }
});
//jdk8 lambda
Page<Country> page = PageHelper.startPage(1, 10).doSelectPage(()-> countryMapper.selectGroupBy());

//return PageInfo
pageInfo = PageHelper.startPage(1, 10).doSelectPageInfo(new ISelect() {
    @Override
    public void doSelect() {
        countryMapper.selectGroupBy();
    }
});
//in lambda
pageInfo = PageHelper.startPage(1, 10).doSelectPageInfo(() -> countryMapper.selectGroupBy());

//do count only
long total = PageHelper.count(new ISelect() {
    @Override
    public void doSelect() {
        countryMapper.selectLike(country);
    }
});
//lambda
total = PageHelper.count(()->countryMapper.selectLike(country));
```  

Introduced The most common ways.

#### 1). RowBounds and PageRowBounds

```java
List<Country> list = sqlSession.selectList("x.y.selectIf", null, new RowBounds(1, 10));
```  
使用这种调用方式时，你可以使用RowBounds参数进行分页，这种方式侵入性最小，我们可以看到，通过RowBounds方式调用只是使用了这个参数，并没有增加其他任何内容。  

分页插件检测到使用了RowBounds参数时，就会对该查询进行<b>物理分页</b>。

关于这种方式的调用，有两个特殊的参数是针对 `RowBounds` 的，你可以参看上面的 **场景一** 和 **场景二**

<b>注：</b>不只有命名空间方式可以用RowBounds，使用接口的时候也可以增加RowBounds参数，例如：  

```java
//这种情况下也会进行物理分页查询
List<Country> selectAll(RowBounds rowBounds);  
```

**注意：** 由于默认情况下的 `RowBounds` 无法获取查询总数，分页插件提供了一个继承自 `RowBounds` 的 `PageRowBounds`，这个对象中增加了 `total` 属性，执行分页查询后，可以从该属性得到查询总数。


#### 2). `PageHelper.startPage` 静态方法调用
除了 `PageHelper.startPage` 方法外，还提供了类似用法的 `PageHelper.offsetPage` 方法。

在你需要进行分页的 MyBatis 查询方法前调用 `PageHelper.startPage` 静态方法即可，紧跟在这个方法后的第一个**MyBatis 查询方法**会被进行分页。  

##### 例一：  

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

##### 例二：
```java
//request: url?pageNum=1&pageSize=10
//支持 ServletRequest,Map,POJO 对象，需要配合 params 参数
PageHelper.startPage(request);
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

##### 例三，使用`PageInfo`的用法：  

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
#### 3). 使用参数方式
想要使用参数方式，需要配置 `supportMethodsArguments` 参数为 `true`，同时要配置 `params` 参数。
例如下面的配置：
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
在 MyBatis 方法中：
```java
List<Country> selectByPageNumSize(
        @Param("user") User user,
        @Param("pageNumKey") int pageNum, 
        @Param("pageSizeKey") int pageSize);
```
当调用这个方法时，由于同时发现了 `pageNumKey` 和 `pageSizeKey` 参数，这个方法就会被分页。params 提供的几个参数都可以这样使用。

除了上面这种方式外，如果 User 对象中包含这两个参数值，也可以有下面的方法：
```java
List<Country> selectByPageNumSize(User user);
```
当从 User 中同时发现了 `pageNumKey` 和 `pageSizeKey` 参数，这个方法就会被分页。

注意：`pageNum` 和 `pageSize` 两个属性同时存在才会触发分页操作，在这个前提下，其他的分页参数才会生效。


#### 3). `PageHelper` 安全调用

##### 1. 使用 `RowBounds` 和 `PageRowBounds` 参数方式是极其安全的
 
##### 2. 使用参数方式是极其安全的

##### 3. 使用 ISelect 接口调用是极其安全的

ISelect 接口方式除了可以保证安全外，还特别实现了将查询转换为单纯的 count 查询方式，这个方法可以将任意的查询方法，变成一个 `select count(*)` 的查询方法。

##### 4. 什么时候会导致不安全的分页？

`PageHelper` 方法使用了静态的 `ThreadLocal` 参数，分页参数和线程是绑定的。

只要你可以保证在 `PageHelper` 方法调用后紧跟 MyBatis 查询方法，这就是安全的。因为 `PageHelper` 在 `finally` 代码段中自动清除了 `ThreadLocal` 存储的对象。

如果代码在进入 `Executor` 前发生异常，就会导致线程不可用，这属于人为的 Bug（例如接口方法和 XML 中的不匹配，导致找不到 `MappedStatement` 时），
这种情况由于线程不可用，也不会导致 `ThreadLocal` 参数被错误的使用。

但是如果你写出下面这样的代码，就是不安全的用法：
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

上面这个代码，应该写成下面这个样子：
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

如果你对此不放心，你可以手动清理 `ThreadLocal` 存储的分页参数，可以像下面这样使用：
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

### 4. MyBatis 和 Spring 集成示例

如果和Spring集成不熟悉，可以参考下面两个

<b>只有基础的配置信息，没有任何现成的功能，作为新手入门搭建框架的基础</b>

- [集成 Spring 3.x](https://github.com/abel533/Mybatis-Spring/tree/spring3.x)
- [集成 Spring 4.x](https://github.com/abel533/Mybatis-Spring)

这两个集成框架集成了 PageHelper 和 [通用 Mapper](https://github.com/abel533/Mapper)。

### 5. Spring Boot 待定