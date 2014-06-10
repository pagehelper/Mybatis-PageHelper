#Mybatis分页插件 - PageHelper说明  
<br/>  
##最新版为3.2.1 版  

如果你也在用Mybatis，建议尝试该分页插件，这个一定是<b>最方便</b>使用的分页插件。  

该插件目前支持`Oracle`,`Mysql`,`Hsqldb`三种数据库分页。  

##关于startPage方法和RowBounds方式

这个方法的参数为`pageNum`和`pageSize`，`pageNum`为第几页，`pageSize`为每页数量，在大多数前台框架中，使用这两个参数比较方便。  

但是这种方式和`RowBounds`不一致，如果不了解这种区别就会出错，`RowBounds`中的参数是`offset`和`limit`，`limit`和`pageSize`一样，`offset`和`pageNum`<b>很不一样</b>，`offset`是起始的行号，是从几个开始，而`pageNum`是起始的页码。  

`offset = (pageNum-1)*pageSize`  

由于这种不同的存在，可能会导致一些意外出现，<b>因而下一步增加一个可配置参数来指定RowBounds参数offset是否作为pageNum使用</b>  


##多数据库支持   

1. 感谢[鲁家宁][2]增加的对`Mysql`的支持   

2. 增加对`Hsqldb`的支持，方便测试使用  

3. 欢迎各位提供其他数据库版本的分页插件  

<br/><br/>
##相关链接

Mybatis-Sample（分页插件测试项目）：[http://git.oschina.net/free/Mybatis-Sample][3]

Mybatis项目：https://github.com/mybatis/mybatis-3

Mybatis文档：http://mybatis.github.io/mybatis-3/zh/index.html  

Mybatis专栏： 

- [Mybatis示例][4]

- [Mybatis问题集][5]  

作者博客：  

- [http://my.oschina.net/flags/blog][6]

- [http://blog.csdn.net/isea533][7]  

<br/><br/>
##使用方法  

将本插件中的两个类`Page.java`和`PageHelper.java`放到项目中。  

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
    <!-- packageName为PageHelper类所在包名 -->
	<plugin interceptor="packageName.PageHelper">
        <property name="dialect" value="mysql"/>
        <!-- 该参数默认为false -->
        <!-- 设置为true时，会将RowBounds第一个参数offset当成pageNum页码使用 -->
        <!-- 和startPage中的pageNum效果一样-->
        <property name="offsetAsPageNum" value="true"/>
        <!-- 该参数默认为false -->
        <!-- 设置为true时，使用RowBounds分页会进行count查询 -->
        <property name="rowBoundsWithCount" value="true"/>
	</plugin>
</plugins>
```   
这里的`PageHelper`要使用完整的类路径，需要加上包路径。  

增加`dialect`属性，使用时必须指定该属性，可选值为`oracle`,`mysql`,`hsqldb`,<b>没有默认值，必须指定该属性</b>。


###不支持的情况   

对于<b>关联结果查询</b>，使用分页得不到正常的结果，因为只有把数据全部查询出来，才能得到最终的结果，对这个结果进行分页才有效（<i>Mybatis自带的内存分页也无法对这种情况进行正确的分页</i>）。因而如果是这种情况，必然要先全部查询，在对结果处理，这样就体现不出分页的作用了。     
   
相关内容:[Mybatis关联结果查询分页方法][8]  

<br/><br/><br/><br/>
##Mybatis-Sample项目 

这个项目是一个分页插件的测试项目，使用Maven构建，该项目目前提供了4种基本使用方式的测试用例，需要测试Mybatis分页插件的可以clone该项目，该项目中的PageHelper.java和Page<E>两个类不能保证随时和当前项目同步更新，使用时请注意！

项目地址：[http://git.oschina.net/free/Mybatis-Sample][9]

<br/><br/><br/><br/>
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
因为新增了一个Mybatis-Sample项目，所以这里的示例只是简短的一部分，需要更丰富的示例，请查看[Mybatis-Sample][10]项目

###对于两种分页方式如何选择   

1. 如果你不想在Mapper方法上增加一个带`RowBounds`参数的方法，并且你喜欢用Mapper接口形式调用，你可以使用`PageHelper.startPage`，并且该方法可以控制是否执行count方法。  

2. 实际上在Mapper接口中添加一个带`RowBounds`参数的方法很容易，使用和不带`RowBounds`参数一样的xml就可以。  

3. 如果你喜欢使用`sqlSession.selectList`这种命名空间方式的调用，使用`RowBounds`会更方便。

###关于MappedStatement  
```java
    MappedStatement qs = newMappedStatement(ms, new BoundSqlSqlSource(boundSql));
```
这段代码执行100万次耗时在1.5秒（测试机器：CPU酷睿双核T6600，4G内存）左右，因而不考虑对该对象进行缓存等考虑  

<br/><br/><br/><br/>
##更新日志   

###v3.2.1

1. 新增`offsetAsPageNum`参数，用来控制`RowBounds`中的`offset`是否作为`pageNum`使用，`pageNum`和`startPage`中的含义相同，`pageNum`是页码。该参数默认为`false`，使用默认值时，不需要配置该参数。

2. 新增`rowBoundsWithCount`参数，用来控制使用`RowBounds`时是否执行`count`查询。该参数默认为`false`，使用默认值时，不需要配置该参数。

###v3.2.0

1. 增加了对`Hsqldb`的支持，主要目的是为了方便测试使用`Hsqldb`  

2. 增加了该项目的一个测试项目[Mybatis-Sample][11]，测试项目数据库使用`Hsqldb`  

3. 增加MIT协议

###v3.1.2

1. 解决count sql在`oracle`中的错误

###v3.1.1 
 
1. 统一返回值为`Page<E>`（可以直接按`List`使用）,方便在页面使用EL表达式，如`${page.pageNum}`,`${page.total}`     
   
###v3.1.0
  
1. 解决了`RowBounds`分页的严重BUG，原先会在物理分页基础上进行内存分页导致严重错误，已修复  

2. 增加对MySql的支持，该支持由[鲁家宁][12]增加。  
  
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


  [1]: http://git.oschina.net/free/Mybatis_PageHelper/issues/4
  [2]: http://my.oschina.net/lujianing
  [3]: http://git.oschina.net/free/Mybatis-Sample
  [4]: http://blog.csdn.net/column/details/mybatis-sample.html
  [5]: http://blog.csdn.net/column/details/mybatisqa.html
  [6]: http://my.oschina.net/flags/blog
  [7]: http://blog.csdn.net/isea533
  [8]: http://my.oschina.net/flags/blog/274000
  [9]: http://git.oschina.net/free/Mybatis-Sample
  [10]: http://git.oschina.net/free/Mybatis-Sample
  [11]: http://git.oschina.net/free/Mybatis-Sample
  [12]: http://my.oschina.net/lujianing