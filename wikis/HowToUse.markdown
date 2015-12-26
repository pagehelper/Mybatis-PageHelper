##使用方法  

###1. 引入分页插件  

引入分页插件一共有下面2种方式，推荐使用Maven方式，这种方式方便更新。  

####1). 引入Jar包  

如果你想使用本项目的jar包而不是直接引入类，你可以在这里下载各个版本的jar包（点击Download下的jar即可下载）  

 - https://oss.sonatype.org/content/repositories/releases/com/github/pagehelper/pagehelper/
 
 - http://repo1.maven.org/maven2/com/github/pagehelper/pagehelper/

由于使用了sql解析工具，你还需要下载jsqlparser.jar（这个文件完全独立，不依赖其他）：  

 - http://repo1.maven.org/maven2/com/github/jsqlparser/jsqlparser/0.9.1/
 
 - http://git.oschina.net/free/Mybatis_PageHelper/attach_files

####2). 使用maven  
  
添加如下依赖：  

```xml  
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper</artifactId>
    <version>4.0.1</version>
</dependency>
```  

当使用maven中央库中的快照版（带`"-SNAPSHOT"`的版本）时，需要在pom.xml中添加如下配置：  

```xml
<repositories>
    <repository>
          <id>sonatype-nexus-snapshots</id>
          <name>Sonatype Nexus Snapshots</name>
          <url>http://oss.sonatype.org/content/repositories/snapshots</url>
          <releases>
                <enabled>false</enabled>
          </releases>
          <snapshots>
                <enabled>true</enabled>
          </snapshots>
    </repository>
</repositories>
```

###2. 在Mybatis配置xml中配置拦截器插件:    

```xml
<!-- 
    plugins在配置文件中的位置必须符合要求，否则会报错，顺序如下:
    properties?, settings?, 
    typeAliases?, typeHandlers?, 
    objectFactory?,objectWrapperFactory?, 
    plugins?, 
    environments?, databaseIdProvider?, mappers?
-->
<plugins>
    <!-- com.github.pagehelper为PageHelper类所在包名 -->
    <plugin interceptor="com.github.pagehelper.PageHelper">
        <!-- 4.0.0以后版本可以不设置该参数 -->
        <property name="dialect" value="mysql"/>
        <!-- 该参数默认为false -->
        <!-- 设置为true时，会将RowBounds第一个参数offset当成pageNum页码使用 -->
        <!-- 和startPage中的pageNum效果一样-->
        <property name="offsetAsPageNum" value="true"/>
        <!-- 该参数默认为false -->
        <!-- 设置为true时，使用RowBounds分页会进行count查询 -->
        <property name="rowBoundsWithCount" value="true"/>
        <!-- 设置为true时，如果pageSize=0或者RowBounds.limit = 0就会查询出全部的结果 -->
        <!-- （相当于没有执行分页查询，但是返回结果仍然是Page类型）-->
        <property name="pageSizeZero" value="true"/>
        <!-- 3.3.0版本可用 - 分页参数合理化，默认false禁用 -->
        <!-- 启用合理化时，如果pageNum<1会查询第一页，如果pageNum>pages会查询最后一页 -->
        <!-- 禁用合理化时，如果pageNum<1或pageNum>pages会返回空数据 -->
        <property name="reasonable" value="false"/>
        <!-- 3.5.0版本可用 - 为了支持startPage(Object params)方法 -->
        <!-- 增加了一个`params`参数来配置参数映射，用于从Map或ServletRequest中取值 -->
        <!-- 可以配置pageNum,pageSize,count,pageSizeZero,reasonable,orderBy,不配置映射的用默认值 -->
        <!-- 不理解该含义的前提下，不要随便复制该配置 -->
        <property name="params" value="pageNum=pageHelperStart;pageSize=pageHelperRows;"/>
        <!-- 支持通过Mapper接口参数来传递分页参数 -->
        <property name="supportMethodsArguments" value="false"/>
        <!-- always总是返回PageInfo类型,check检查返回类型是否为PageInfo,none返回Page -->
        <property name="returnPageInfo" value="none"/>
	</plugin>
</plugins>
```

这里的`com.github.pagehelper.PageHelper`使用完整的类路径。

其他五个参数说明：

1. 增加`dialect`属性，使用时可以指定该属性（<b>不指定的情况下，分页插件会自动判断</b>），可选值为`oracle`,`mysql`,`mariadb`,`sqlite`,`hsqldb`,`postgresql`,`db2`,`sqlserver`,`informix`,`h2`,`sqlserver2012`。

2. 增加`offsetAsPageNum`属性，默认值为`false`，使用默认值时不需要增加该配置，需要设为`true`时，需要配置该参数。当该参数设置为`true`时，使用`RowBounds`分页时，会将`offset`参数当成`pageNum`使用，可以用页码和页面大小两个参数进行分页。

3. 增加`rowBoundsWithCount`属性，默认值为`false`，使用默认值时不需要增加该配置，需要设为`true`时，需要配置该参数。当该参数设置为`true`时，使用`RowBounds`分页会进行count查询。

4. 增加`pageSizeZero`属性，默认值为`false`，使用默认值时不需要增加该配置，需要设为`true`时，需要配置该参数。当该参数设置为`true`时，如果`pageSize=0`或者`RowBounds.limit = 0`就会查询出全部的结果（相当于没有执行分页查询，但是返回结果仍然是`Page`类型）。

5. 增加`reasonable`属性，默认值为`false`，使用默认值时不需要增加该配置，需要设为`true`时，需要配置该参数。具体作用请看上面配置文件中的注释内容。

6. 为了支持`startPage(Object params)`方法，增加了一个`params`参数来配置参数映射，用于从Map或ServletRequest中取值，可以配置pageNum,pageSize,count,pageSizeZero,reasonable,orderBy,不配置映射的用默认值。

7. `supportMethodsArguments`支持通过Mapper接口参数来传递分页参数，默认值`false`，具体用法参考`com.github.pagehelper.test.basic`包下的`ArgumentsMapTest`和`ArgumentsObjTest`测试类。

8. `returnPageInfo`用来支持直接返回`PageInfo`类型，默认值`none`，可选参数always总是返回PageInfo类型,check检查返回类型是否为PageInfo,none返回Page(List)类型。用法和配置参考`com.github.pagehelper.test.basic`包下的`PageInfoTest`，特别要注意接口的返回值和xml中的`resultType`类型。

<b>重要提示：</b>

当`offsetAsPageNum=false`的时候，由于PageNum问题，`RowBounds`查询的时候reasonable会强制为false。使用`PageHelper.startPage`方法不受影响。

另外使用`RowBounds`在这种情况下返回的`Page`对象由于没有正确的`pageNum`属性，所以也不能使用`PageInfo`处理。

如果你不理解为什么，可以看这样一个例子：查询`offset=7,limit=10`，这个时候`pageNum=?`，这种情况没法计算`pageNum`，没法判断当前是第几页。

###3. 如何选择配置这些参数

单独看每个参数的说明可能是一件让人不爽的事情，这里列举一些可能会用到某些参数的情况。

首先`dialect`属性是必须的，不需要解释。其他的参数一般情况下我们都不必去管，如果想了解何时使用合适，你可以参考以下场景：

####场景一

如果你仍然在用类似ibatis式的命名空间调用方式，你也许会用到`rowBoundsWithCount`，分页插件对`RowBounds`支持和Mybatis默认的方式是一致，默认情况下不会进行count查询，如果你想在分页查询时进行count查询，以及使用更强大的`PageInfo`类，你需要设置该参数为`true`。

####场景二

如果你仍然在用类似ibatis式的命名空间调用方式，你觉得RowBounds中的两个参数`offset,limit`不如`pageNum,pageSize`容易理解，你可以使用`offsetAsPageNum`参数，将该参数设置为`true`后，`offset`会当成`pageNum`使用，`limit`和`pageSize`含义相同。

####场景三

如果觉得某个地方使用分页后，你仍然想通过控制参数查询全部的结果，你可以配置`pageSizeZero`为`true`，配置后，如可以通过设置`pageSize=0`或者`RowBounds.limit = 0`就会查询出全部的结果。

####场景四

如果你分页插件使用于类似分页查看列表式的数据，如新闻列表，软件列表，你希望用户输入的页数不在合法范围（第一页到最后一页之外）时能够正确的响应到正确的结果页面，那么你可以配置`reasonable`为`true`，这时如果`pageNum<1`会查询第一页，如果`pageNum>总页数`会查询最后一页。

###4. Spring配置方法  

首先需要在Spring中配置`org.mybatis.spring.SqlSessionFactoryBean`。然后配置配置Mybatis的具体配置有两种方式，一种是用mybatis默认的xml配置，另一种就是完全使用spring的属性配置方式。

####1.mybatis默认的xml配置

配置`configLocation`属性指向上面的`mybatis-config.xml`文件。有关分页插件的配置都在`mybatis-config.xml`，具体配置内容参考上面的`mybatis-config.xml`。  

####2.使用spring的属性配置方式

<b>注意：请不用同时使用spring配置方式和mybatis-config.xml配置方式，只需要选择其中一个就行。配置多个分页插件时，会抛出异常提示。</b>

>分页插件配置错误:请不要在系统中配置多个分页插件(使用Spring时,mybatis-config.xml和Spring<bean>配置方式，请选择其中一种，不要同时配置多个分页插件)！

使用spring的属性配置方式，可以使用`plugins`属性像下面这样配置：    

```xml
<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
  <property name="dataSource" ref="dataSource"/>
  <property name="mapperLocations">
    <array>
      <value>classpath:mapper/*.xml</value>
    </array>
  </property>
  <property name="typeAliasesPackage" value="com.isea533.ssm.model"/>
  <property name="plugins">
    <array>
      <bean class="com.github.pagehelper.PageHelper">
        <property name="properties">
          <value>
            dialect=hsqldb
          </value>
        </property>
      </bean>
    </array>
  </property>
</bean>
```   

属性配置按照上面的方式配置，每个配置独立一行即可。

###5. 如何在代码中使用  

阅读前后请注意看[重要提示](http://git.oschina.net/free/Mybatis_PageHelper/blob/master/wikis/Important.markdown)

首先该分页插件支持以下两种调用方式：  

```java
//第一种，RowBounds方式的调用
List<Country> list = sqlSession.selectList("x.y.selectIf", null, new RowBounds(1, 10));

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

关于这种方式的调用，有两个特殊的参数是针对`RowBounds`的，你可以参看上面的<a href="#场景一">场景一</a>和<a href="#场景二">场景二</a>

<b>注：</b>不只有命名空间方式可以用RowBounds，使用接口的时候也可以增加RowBounds参数，例如：  

```java
//这种情况下也会进行物理分页查询
List<Country> selectAll(RowBounds rowBounds);  
```

####2). `PageHelper.startPage`静态方法调用
 
在你需要进行分页的Mybatis方法前调用`PageHelper.startPage`静态方法即可，紧跟在这个方法后的第一个<b>Mybatis查询方法</b>会被进行分页。  

#####例一：  

```java
SqlSession sqlSession = MybatisHelper.getSqlSession();
CountryMapper countryMapper = sqlSession.getMapper(CountryMapper.class);
try {
    //获取第1页，10条内容，默认查询总数count
    PageHelper.startPage(1, 10);
    
    //紧跟着的第一个select方法会被分页
    List<Country> list = countryMapper.selectIf(1);
    assertEquals(2, list.get(0).getId());
    assertEquals(10, list.size());
    //分页时，实际返回的结果list类型是Page<E>，如果想取出分页信息，需要强制转换为Page<E>，
    //或者使用PageInfo类（下面的例子有介绍）
    assertEquals(182, ((Page) list).getTotal());
} finally {
    sqlSession.close();
}
```

#####例二：
```java
SqlSession sqlSession = MybatisHelper.getSqlSession();
CountryMapper countryMapper = sqlSession.getMapper(CountryMapper.class);
try {
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
} finally {
    sqlSession.close();
}
```  

#####例三，使用`PageInfo`的用法：  

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

本项目中包含大量测试，您可以通过查看测试代码了解使用方法。  

测试代码地址：http://git.oschina.net/free/Mybatis_PageHelper/tree/master/src/test/java/com/github/pagehelper/test

##MyBatis和Spring集成示例

如果和Spring集成不熟悉，可以参考下面两个

<b>只有基础的配置信息，没有任何现成的功能，作为新手入门搭建框架的基础</b>

- [集成Spring3.x](https://github.com/abel533/Mybatis-Spring)

- [集成Spring4.x](https://github.com/abel533/Mybatis-Spring/tree/spring4)

这两个集成框架集成了MyBatis分页插件和MyBatis通用Mapper。