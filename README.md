#Mybatis分页插件 - PageHelper说明   

如果你也在用Mybatis，建议尝试该分页插件，这个一定是<b>最方便</b>使用的分页插件。  

该插件目前支持`Oracle`,`Mysql`,`Hsqldb`,`PostgreSQL`四种数据库分页。  

[点击提交BUG][1]

##分页插件QQ群：211286137

为了方便收集分页插件（以及Mybatis）方面的问题，新建了一个QQ群方便交流问题。

本身我不喜欢太多的QQ群，所以想加一个现有人多的Mybatis群，由于群的目的不同，而且群内多是一些毫无基础的初学者（基础还是要自己好好学的，一上来就问各种基础问题效果并不好），所以不得不自己建群。


##最新稳定版为3.2.3 版
  
3.2.3版本使用方法请切换到3.2.3版标签查看

地址：[点击进入gitosc-3.2.3目录][2] | [点击进入github-3.2.3目录][3] 

#最新测试版3.3.0-SNAPSHOT

##重要提示：`fdb-sql-parser`换为`jsqlparser`  

为了去掉count查询中的order by语句，最早使用了`fdb-sql-parser`，由于效果不好，现在已经替换成`jsqlparser`，`jsqlparser`比`fdb-sql-parser`更通用，而且体积更小，对原sql改动更少。替换后，下面的有关说明都会改为`jsqlparser`，如果你使用了最新的测试版分页，你需要下载`jsqlparser`。  


##3.3.0-SNAPSHOT改进内容

 1. 对`MappedStatement`对象进行缓存，包括count查询的`MappedStatement`以及分页查询的`MappedStatement`，分页查询改为预编译查询。

 2. 独立的`SqlUtil`类，由于原来的`PageHelper`太复杂，因此将拦截器外的其他代码独立到`SqlUtil`中，方便查看代码和维护。`SqlUtil`中增加`Parser`接口，提供一个抽象的`SimpleParser`实现，不同数据库的分页代码通过继承`SimpleParser`实现。

 3. 特殊的`Parser`实现类`SqlParser`类，这是一个独立的java类，主要提供了更高性能的count查询sql，可以根据sql自动改为`count(*)`查询，自动去除不需要的`order by`语句，如果需要使用该类，只要把该类放到`SqlUtil`类相同的包下即可，同时需要引入Jar包`jsqlparser-0.9.1.jar`。

 4. 增强的`PageInfo`类，`PageInfo`类包含了分页几乎所有需要用到的属性值，减少了对分页逻辑的过多投入。  

 4. 分页合理化，自动处理pageNum的异常情况。例如当pageNum<=0时，会设置pageNum=1，然后查询第一页。当pageNum>pages(总页数)时，自动将pageNum=pages，查询最后一页。  

 5. 增加对`PostgreSQL`支持。

##使用方法  

###1. 引入分页代码或Jar包或使用Maven  

####(1). 将本插件中的`com.github.pagehelper`包（[点击进入gitosc包][4] | [点击进入github包][5]）下面的三个类`Page`,`PageHelper`和`SqlUtil`放到项目中，如果需要使用`PageInfo`（强大的分页包装类），也可以放到项目中。如果想使用更高效的`count`查询，你也可以将`SqlParser`放到`SqlUtil`相同的包下，使用`SqlParser`时必须使用`jsqlparser-0.9.1.jar`。  

代码文件说明：  
  
 - `Page<E>`\[必须\]：分页参数类，该类继承`ArrayList`，虽然分页查询返回的结果实际类型是`Page<E>`,但是可以完全不出现所有的代码中，可以直接当成`List`使用。返回值不建议使用`Page`，建议仍然用`List`。如果需要用到分页信息，使用下面的`PageInfo`类对List进行包装即可。  
 
 - `PageHelper`\[必须\]：分页插件拦截器类，对Mybatis的拦截在这个类中实现。
 
 - `PageInfo`\[可选\]：`Page<E>`的包装类，包含了全面的分页属性信息。  
 
 - `SqlParser`\[可选\]：提供高效的count查询sql。主要是智能替换原sql语句为count(*)，去除不带参数的order by语句。需要`jsqlparser-0.9.1.jar`支持。  
 
 - `SqlUtil`\[必须\]：分页插件工具类，分页插件逻辑类，分页插件的主要实现方法都在这个类中。  
  
####(2). 如果你想使用本项目的jar包而不是直接引入类，你可以在这里下载各个版本的jar包（点击Download下的jar即可下载）  

 - https://oss.sonatype.org/#nexus-search;quick~pagehelper  

由于使用了sql解析工具，你还需要下载这个文件（这个文件完全独立，不依赖其他）：  

 - SqlParser.jar：http://search.maven.org/remotecontent?filepath=com/github/jsqlparser/jsqlparser/0.9.1/jsqlparser-0.9.1.jar
 
 - SqlParser - github地址：https://github.com/JSQLParser/JSqlParser  

<br>

####(3). 如果你使用的maven，你可以添加如下依赖：  

```xml  
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper</artifactId>
    <version>3.3.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>com.github.jsqlparser</groupId>
    <artifactId>jsqlparser</artifactId>
    <version>0.9.1</version>
    <optional>true</optional>
</dependency>
```  

使用maven中央库中的快照版时，需要在pom.xml中添加如下配置：  

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
        <property name="reasonable" value="true"/>
	</plugin>
</plugins>
```   

这里的`com.github.pagehelper.PageHelper`使用完整的类路径。  

其他五个参数说明：

1. 增加`dialect`属性，使用时必须指定该属性，可选值为`oracle`,`mysql`,`hsqldb`,`postgresql`,<b>没有默认值，必须指定该属性</b>。  

2. 增加`offsetAsPageNum`属性，默认值为`false`，使用默认值时不需要增加该配置，需要设为`true`时，需要配置该参数。当该参数设置为`true`时，使用`RowBounds`分页时，会将`offset`参数当成`pageNum`使用，可以用页码和页面大小两个参数进行分页。  

3. 增加`rowBoundsWithCount`属性，默认值为`false`，使用默认值时不需要增加该配置，需要设为`true`时，需要配置该参数。当该参数设置为`true`时，使用`RowBounds`分页会进行count查询。  

4. 增加`pageSizeZero`属性，默认值为`false`，使用默认值时不需要增加该配置，需要设为`true`时，需要配置该参数。当该参数设置为`true`时，如果`pageSize=0`或者`RowBounds.limit = 0`就会查询出全部的结果（相当于没有执行分页查询，但是返回结果仍然是`Page`类型）。  

5. 增加`reasonable`属性，默认值为`false`，使用默认值时不需要增加该配置，需要设为`true`时，需要配置该参数。具体作用请看上面配置文件中的注释内容。  

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
            reasonable=true
          </value>
        </property>
      </bean>
    </array>
  </property>
</bean>
```   

属性配置按照上面的方式配置，每个配置独立一行即可。

##分页示例：  

```java
SqlSession sqlSession = MybatisHelper.getSqlSession();
CountryMapper countryMapper = sqlSession.getMapper(CountryMapper.class);
try {
    //获取第1页，10条内容，默认查询总数count
    PageHelper.startPage(1, 10);
    List<Country> list = countryMapper.selectIf(1);
    assertEquals(2, list.get(0).getId());
    assertEquals(10, list.size());
    assertEquals(182, ((Page) list).getTotal());

    //获取第1页，10条内容，默认查询总数count
    PageHelper.startPage(1, 10);
    list = countryMapper.selectIf(null);
    assertEquals(1, list.get(0).getId());
    assertEquals(10, list.size());
    assertEquals(183, ((Page) list).getTotal());
} finally {
    sqlSession.close();
}
```  

使用`PageInfo`的用法：  

```java
//获取第1页，10条内容，默认查询总数count
PageHelper.startPage(1, 10);
List<Country> list = countryMapper.selectAll();
//用PageInfo对结果进行包装
PageInfo page = new PageInfo(list);
//测试PageInfo全部属性
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

<br/>
##Mybatis-Sample项目 

这个项目是一个分页插件的WEB测试项目，使用Maven构建，只包含一个简单的例子和简单的页面分页效果。

项目地址：[http://git.oschina.net/free/Mybatis-Sample][6]

<br/>

##对于两种分页方式如何选择   

两种方式指的是如下代码所示的两种：  

```java
//第一种，命名空间方式的调用
List<Country> list = sqlSession.selectList("selectIf", null, new RowBounds(1, 10));

//第二种，Mapper接口方式的调用，推荐这种使用方式。
PageHelper.startPage(1, 10);
List<Country> list = countryMapper.selectIf(1);
```

1. 如果你不想在Mapper方法上增加一个带`RowBounds`参数的方法，并且你喜欢用Mapper接口形式调用，你可以使用`PageHelper.startPage`，并且该方法可以控制是否执行count方法。  

2. 实际上在Mapper接口中添加一个带`RowBounds`参数的方法很容易，使用和不带`RowBounds`参数一样的xml就可以。  

3. 如果你喜欢使用`sqlSession.selectList`这种命名空间方式的调用，使用`RowBounds`会更方便。

<br/>

##`PageHelper.startPage`方法重要提示

只有紧跟在`PageHelper.startPage`方法后的<b>第一个</b>Mybatis的<b>查询（Select方法）</b>方法会被分页。

<br/>

##分页插件不支持带有`for update`语句的分页

对于带有`for update`的sql，会抛出运行时异常，对于这样的sql建议手动分页，毕竟这样的sql需要重视。

<br/>

##分页插件不支持关联结果查询

原因以及解决方法可以看这里：
>http://my.oschina.net/flags/blog/274000 

分支插件不支持关联结果查询，但是支持关联嵌套查询。只会对主sql进行分页，嵌套的sql不会被分页。  

<br/>

##`SqlUtil.testSql`测试sql方法  

为了便于测试sql语句方面的问题，提供了`SqlUtil.testSql`方法，使用方法如下：  

```java
String originalSql = "Select * from `order` o where abc = ? order by id desc , name asc";
SqlUtil.testSql("mysql", originalSql);
SqlUtil.testSql("oracle", originalSql);
```  

执行后输出： 

```sql   
SELECT count(*) FROM sys_user o WHERE abc = ?
select * from (Select * from sys_user o where abc = ? order by id desc , name asc) as tmp_page limit ?,?

SELECT count(*) FROM sys_user o WHERE abc = ?
select * from ( select tmp_page.*, rownum row_id from ( Select * from sys_user o where abc = ? order by id desc , name asc ) tmp_page where rownum <= ? ) where row_id > ?
```  

使用`SqlParser`和不使用`SqlParser`的count查询sql很不一样。`SqlParser`可以更智能将原sql改为count查询，并且去除order by。

例如不使用`SqlParser`时，和上面相同原sql的输出：

```sql
select count(*) from (Select * from sys_user o where abc = ? order by id desc , name asc) tmp_count
select * from (Select * from sys_user o where abc = ? order by id desc , name asc) as tmp_page limit ?,?

select count(*) from (Select * from sys_user o where abc = ? order by id desc , name asc) tmp_count
select * from ( select tmp_page.*, rownum row_id from ( Select * from sys_user o where abc = ? order by id desc , name asc ) tmp_page where rownum <= ? ) where row_id > ?
```

##分页插件多数据库测试  

为了更方便的测试不同的数据库，在`src/test/resources`目录下增加了不同数据库的mybatis配置文件，通过修改`test.properties`中的配置可以让测试使用不同的配置进行测试。  

`test.properties`内容：  

```properties
#首先需要在本机配置对应的数据库

#想要测试那个数据库，这里就写那个数据库
#这个值和test/resources中的数据库对应的文件夹名字相同
#目前可选为:
#hsqldb
#mysql
#oracle
#postgresql
database = hsqldb
```  
各种数据库对应的sql文件都在对应的目录中。

<br/><br/>
##相关链接

对应于oschub的项目地址：http://git.oschina.net/free/Mybatis_PageHelper

对应于github的项目地址：https://github.com/pagehelper/Mybatis-PageHelper

Mybatis-Sample（分页插件测试项目）：[http://git.oschina.net/free/Mybatis-Sample][7]

Mybatis项目：https://github.com/mybatis/mybatis-3

Mybatis文档：http://mybatis.github.io/mybatis-3/zh/index.html  

Mybatis专栏： 

- [Mybatis示例][8]

- [Mybatis问题集][9]  

作者博客：  

- [http://my.oschina.net/flags/blog][10]

- [http://blog.csdn.net/isea533][11]  

作者QQ： 120807756  

作者邮箱： abel533@gmail.com  

<br/><br/>
##更新日志   

###v3.2.3  

1. 解决`mysql`带有`for update`时分页错误的问题。  

2. 当`pageSize`（或`RowBounds`的`limit`）`<=0` 时不再进行分页查询，只会进行count查询（RowBounds需要配置进行count查询），相当于用分页查询来做count查询了。  

3. 增加了`pageSizeZero`参数，当`pageSizeZero=true`时，如果`pageSize=0`（或`RowBounds.limit`=0），就会查询全部的结果。这个参数对于那些在特殊情况下要查询全部结果的人有用。配置该参数后会与上面第二条冲突，解决方法就是如果只想查询count，就设置`pageSize<0`（如 `-1`），只要不等于0（或者不配置pageSizeZero）就不会出现全部查询的情况。  

4. 这个版本没有包含count查询时自动去除`order by`的功能，这个功能将会添加到3.3.0版本中。  

5. 为了便于本项目的统一管理和发布，本项目会和github上面同步，项目会改为Maven管理的结构。  


###v3.2.2

1. 简单重构优化代码。  

2. 新增`PageInfo`包装类，对分页结果Page<E>进行封装，方便EL使用。

3. 将`SystemMetaObject`类的`fromObject`方法内置到分页插件中，方便低版本的Mybatis使用该插件。   

###v3.2.1

1. 新增`offsetAsPageNum`参数，用来控制`RowBounds`中的`offset`是否作为`pageNum`使用，`pageNum`和`startPage`中的含义相同，`pageNum`是页码。该参数默认为`false`，使用默认值时，不需要配置该参数。

2. 新增`rowBoundsWithCount`参数，用来控制使用`RowBounds`时是否执行`count`查询。该参数默认为`false`，使用默认值时，不需要配置该参数。

###v3.2.0

1. 增加了对`Hsqldb`的支持，主要目的是为了方便测试使用`Hsqldb`  

2. 增加了该项目的一个测试项目[Mybatis-Sample][12]，测试项目数据库使用`Hsqldb`  

3. 增加MIT协议

###v3.1.2

1. 解决count sql在`oracle`中的错误

###v3.1.1 
 
1. 统一返回值为`Page<E>`（可以直接按`List`使用）,方便在页面使用EL表达式，如`${page.pageNum}`,`${page.total}`     
   
###v3.1.0
  
1. 解决了`RowBounds`分页的严重BUG，原先会在物理分页基础上进行内存分页导致严重错误，已修复  

2. 增加对MySql的支持，该支持由[鲁家宁][13]增加。  
  
###v3.0 
 
1. 现在支持两种形式的分页，使用`PageHelper.startPage`方法或者使用`RowBounds`参数   

2. `PageHelper.startPage`方法修改，原先的`startPage(int pageNum, int pageSize)`默认求count，新增的`startPage(int pageNum, int pageSize, boolean count)`设置`count=false`可以不执行count查询  

3. 移除`endPage`方法，现在本地变量`localPage`改为取出后清空本地变量。  

4. 修改`Page<E>`类，继承`ArrayList<E>`  

5. 关于两种形式的调用，请看示例代码   
    
###v2.1    

1. 解决并发异常  

2. 分页sql改为直接拼sql    

###v2.0  

1. 支持Mybatis缓存，count和分页同时支持（二者同步）  

2. 修改拦截器签名，拦截`Executor`

3. 将`Page<E>`类移到外面，方便调用

###v1.0  

1. 支持`<foreach>`等标签的分页查询  

2. 提供便捷的使用方式  


  [1]: http://git.oschina.net/free/Mybatis_PageHelper/issues/new?issue%5Bassignee_id%5D=&issue%5Bmilestone_id%5D=
  [2]:http://git.oschina.net/free/Mybatis_PageHelper/tree/v3.2.3/
  [3]:https://github.com/pagehelper/Mybatis-PageHelper/tree/v3.2.3/
  [4]: http://git.oschina.net/free/Mybatis_PageHelper/tree/master/src/main/java/com/github/pagehelper
  [5]:https://github.com/pagehelper/Mybatis-PageHelper/tree/master/src/main/java/com/github/pagehelper
  [6]: http://git.oschina.net/free/Mybatis-Sample
  [7]: http://git.oschina.net/free/Mybatis-Sample
  [8]: http://blog.csdn.net/column/details/mybatis-sample.html
  [9]: http://blog.csdn.net/column/details/mybatisqa.html
  [10]: http://my.oschina.net/flags/blog
  [11]: http://blog.csdn.net/isea533
  [12]: http://git.oschina.net/free/Mybatis-Sample
  [13]: http://my.oschina.net/lujianing