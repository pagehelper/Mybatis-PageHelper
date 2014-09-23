#Mybatis分页插件 - PageHelper说明  
<br/>  

#重大项目改动

为了便于本项目的统一管理和发布，本项目会和github上面同步，项目会改为Maven管理的结构。有关的基本测试内容都在本项目中，Mybatis-Sample项目只会保留Web方面的测试。  

由于测试类过多，会对测试类进行更合理的分组。并且会逐步增加mysql和oracle的测试。

#最新测试版3.3.0-SNAPSHOT

听取[@hlevel][1]的建议和[@da老虎](http://my.oschina.net/u/2006157)的建议，对分页插件性能做了优化。  

##3.3.0-SNAPSHOT版本的改进内容

 1. 对`MappedStatement`对象进行缓存，包括count查询的`MappedStatement`以及分页查询的`MappedStatement`，分页查询改为预编译查询。

 2. 对count查询进行优化处理，目前的处理策略只是简单的把sql中的所有`order by`语句删除了，当然不是直接处理字符串去删除，使用了一个sql解析的类库，由于sql的有无限的变化，因而不保证这个sql解析的类库能够完全处理所有的情况，无法处理的情况仍然会保留order by进行查询。  

 3. 增强的PageInfo类，PageInfo类包含了分页几乎所有需要用到的属性值。方便通过一个PageInfo类来达到分页目的，减少对分页逻辑的过多投入。  

 4. 分页合理化，自动处理pageNum的异常情况。例如当pageNum<=0时，会设置pageNum=1，然后查询第一页。当pageNum>pages(总页数)时，自动将pageNum=pages，查询最后一页。  

 5. 特殊的pageSize值，当pageSize<0时不再进行分页查询，只进行count查询。当pageSize=0时，通过配置参数`pageSizeZero`可以查询全部结果。（该功能已经添加到3.2.3版本）

**欢迎大家尝试这个版本，如果存在任何问题欢迎各位提出。**  

##3.3.0-SNAPSHOT使用方法  

将本插件中的`com.github.pagehelper`包下面的两个类`Page.java`和`PageHelper.java`放到项目中，如果需要使用`PageInfo.java`，也可以放到项目中。  

如果你想使用本项目的jar包而不是直接引入类，你可以在这里下载各个版本的jar包（点击Download下的jar即可下载）  

https://oss.sonatype.org/#nexus-search;quick~pagehelper  

由于使用了sql解析工具，你还需要下载这个文件（这个文件完全独立，不依赖其他）：  

 - SqlParser：http://search.maven.org/remotecontent?filepath=com/foundationdb/fdb-sql-parser/1.3.0/fdb-sql-parser-1.3.0.jar  

<br>
如果你使用的maven，你可以添加如下依赖：  
```xml  
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper</artifactId>
    <version>3.3.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>com.foundationdb</groupId>
    <artifactId>fdb-sql-parser</artifactId>
    <version>1.3.0</version>
</dependency>
```  


---------------------
<br>

#最新稳定版为3.2.3 版  

如果你也在用Mybatis，建议尝试该分页插件，这个一定是<b>最方便</b>使用的分页插件。  

该插件目前支持`Oracle`,`Mysql`,`Hsqldb`三种数据库分页。  


##使用方法  

将本插件中的`com.github.pagehelper`包下面的两个类`Page.java`和`PageHelper.java`放到项目中，如果需要使用`PageInfo.java`，也可以放到项目中。    

如果你想使用本项目的jar包而不是直接引入类，你可以在这里下载各个版本的jar包（点击Download下的jar即可下载）：  

http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.github.pagehelper%22%20AND%20a%3A%22pagehelper%22  

或者如果你使用Maven，你可以添加如下依赖：  

```xml
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper</artifactId>
    <version>3.2.3</version>
</dependency>
```

然后在Mybatis的配置xml中配置拦截器插件:    
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
        <!-- 分页参数合理化，默认false禁用 -->
        <!-- 启用合理化时，如果pageNum<1会查询第一页，如果pageNum>pages会查询最后一页 -->
        <!-- 禁用合理化时，如果pageNum<1或pageNum>pages会返回空数据 -->
        <property name="reasonable" value="true"/>
	</plugin>
</plugins>
```   
这里的`com.github.pagehelper.PageHelper`使用完整的类路径。  

其他三个参数说明：

1. 增加`dialect`属性，使用时必须指定该属性，可选值为`oracle`,`mysql`,`hsqldb`,<b>没有默认值，必须指定该属性</b>。  

2. 增加`offsetAsPageNum`属性，默认值为`false`，使用默认值时不需要增加该配置，需要设为`true`时，需要配置该参数。当该参数设置为`true`时，使用`RowBounds`分页时，会将`offset`参数当成`pageNum`使用，可以用页码和页面大小两个参数进行分页。  

3. 增加`rowBoundsWithCount`属性，默认值为`false`，使用默认值时不需要增加该配置，需要设为`true`时，需要配置该参数。当该参数设置为`true`时，使用`RowBounds`分页会进行count查询。  

4. 增加`pageSizeZero`属性，默认值为`false`，使用默认值时不需要增加该配置，需要设为`true`时，需要配置该参数。当该参数设置为`true`时，如果`pageSize=0`或者`RowBounds.limit = 0`就会查询出全部的结果（相当于没有执行分页查询，但是返回结果仍然是`Page`类型）。  

5. 增加`reasonable`属性，默认值为`false`，使用默认值时不需要增加该配置，需要设为`true`时，需要配置该参数。具体作用请看上面配置文件中的注释内容。  

##分页示例：
```java
@Test
public void testPageHelperByStartPage() throws Exception {
    String logip = "";
    String username = "super";
    String loginDate = "";
    String exitDate = null;
    String logerr = null;
    //不进行count查询，第三个参数设为false
    PageHelper.startPage(1, 10, false);
    //返回结果是Page<SysLoginLog>     
    //该对象除了包含返回结果外，还包含了分页信息，可以直接按List使用
    List<SysLoginLog> logs = sysLoginLogMapper
            .findSysLoginLog(logip, username, loginDate, exitDate, logerr);
    Assert.assertEquals(10, logs.size());

    //当第三个参数没有或者为true的时候，进行count查询
    PageHelper.startPage(2, 10);
    //返回结果是Page<SysLoginLog>     
    //该对象除了包含返回结果外，还包含了分页信息，可以直接按List使用
    Page<SysLoginLog> page = (Page<SysLoginLog>) sysLoginLogMapper
            .findSysLoginLog(logip, username, loginDate, exitDate, logerr);
    Assert.assertEquals(10, page.getResult().size());
    //进行count查询，返回结果total>0
    Assert.assertTrue(page.getTotal() > 0);
}
```  
因为新增了一个Mybatis-Sample项目，所以这里的示例只是简短的一部分，需要更丰富的示例，请查看[Mybatis-Sample][3]项目  


<br/>
##Mybatis-Sample项目 

这个项目是一个分页插件的测试项目，使用Maven构建，该项目目前提供了4种基本使用方式的测试用例，需要测试Mybatis分页插件的可以clone该项目。该项目使用了maven配置的该分页插件。

项目地址：[http://git.oschina.net/free/Mybatis-Sample][4]

<br/>

##对于两种分页方式如何选择   

1. 如果你不想在Mapper方法上增加一个带`RowBounds`参数的方法，并且你喜欢用Mapper接口形式调用，你可以使用`PageHelper.startPage`，并且该方法可以控制是否执行count方法。  

2. 实际上在Mapper接口中添加一个带`RowBounds`参数的方法很容易，使用和不带`RowBounds`参数一样的xml就可以。  

3. 如果你喜欢使用`sqlSession.selectList`这种命名空间方式的调用，使用`RowBounds`会更方便。


<br/><br/>
##相关链接

Mybatis-Sample（分页插件测试项目）：[http://git.oschina.net/free/Mybatis-Sample][5]

Mybatis项目：https://github.com/mybatis/mybatis-3

Mybatis文档：http://mybatis.github.io/mybatis-3/zh/index.html  

Mybatis专栏： 

- [Mybatis示例][6]

- [Mybatis问题集][7]  

作者博客：  

- [http://my.oschina.net/flags/blog][8]

- [http://blog.csdn.net/isea533][9]  

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

2. 增加了该项目的一个测试项目[Mybatis-Sample][10]，测试项目数据库使用`Hsqldb`  

3. 增加MIT协议

###v3.1.2

1. 解决count sql在`oracle`中的错误

###v3.1.1 
 
1. 统一返回值为`Page<E>`（可以直接按`List`使用）,方便在页面使用EL表达式，如`${page.pageNum}`,`${page.total}`     
   
###v3.1.0
  
1. 解决了`RowBounds`分页的严重BUG，原先会在物理分页基础上进行内存分页导致严重错误，已修复  

2. 增加对MySql的支持，该支持由[鲁家宁][11]增加。  
  
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


  [1]: http://my.oschina.net/hlevel
  [2]: http://git.oschina.net/free/Mybatis-Sample
  [3]: http://git.oschina.net/free/Mybatis-Sample
  [4]: http://git.oschina.net/free/Mybatis-Sample
  [5]: http://git.oschina.net/free/Mybatis-Sample
  [6]: http://blog.csdn.net/column/details/mybatis-sample.html
  [7]: http://blog.csdn.net/column/details/mybatisqa.html
  [8]: http://my.oschina.net/flags/blog
  [9]: http://blog.csdn.net/isea533
  [10]: http://git.oschina.net/free/Mybatis-Sample
  [11]: http://my.oschina.net/lujianing