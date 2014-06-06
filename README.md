#Mybatis分页插件 - PageHelper说明  

###最新版为3.2.0 版  

如果你也在用Mybatis，建议尝试该分页插件，这个一定是<b>最方便</b>使用的分页插件。  

该插件目前支持`Oracle`,`Mysql`,`Hsqldb`。  

**说明**：  

这个分页插件并不想成为一个很复杂的项目，只想用几个（目前两个）简单的类来提供分页的功能，实际上只要数据库有支持的方法，分页不会太难，只要代码简单，对任何人来说修改都是很容易的事情。  
  
感谢大家的支持，为了方便测试，下一步会添加一个Maven项目，增加一些示例和测试，方便熟悉代码和测试该项目，该项目会独立存在，不会增加到该项目中（<i>你们不觉得就两个类，进到当前页面就可以方便的点开两个类查看不更好吗？</i>）。  

**注**：   

1. 感谢[鲁家宁][1]增加的对`Mysql`的支持   
2. 增加对`Hsqldb`的支持，方便测试使用
3. 欢迎各位提供其他数据库版本的分页插件  

Mybatis-Sample（分页插件测试项目）：[http://git.oschina.net/free/Mybatis-Sample][7]

Mybatis项目：https://github.com/mybatis/mybatis-3

Mybatis文档：http://mybatis.github.io/mybatis-3/zh/index.html  

Mybatis专栏：  

- [Mybatis示例][2]
- [Mybatis问题集][3]  

作者博客：  

- [http://my.oschina.net/flags/blog][4]
- [http://blog.csdn.net/isea533][5]  

###使用方法
在Mybatis的配置xml中配置拦截器插件:    
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
	</plugin>
</plugins>
```   
这里的PageHelper要使用完整的类路径，需要加上包路径。
增加dialect属性，使用时必须指定该属性，可选值为`oracle`,`mysql`,`hsqldb`,<b>没有默认值，必须指定该属性</b>。


###不支持的情况   

对于<b>关联结果查询</b>，使用分页得不到正常的结果，因为只有把数据全部查询出来，才能得到最终的结果，对这个结果进行分页才有效（<i>Mybatis自带的内存分页也无法对这种情况进行正确的分页</i>）。因而如果是这种情况，必然要先全部查询，在对结果处理，这样就体现不出分页的作用了。   

对于<b>关联嵌套查询</b>，使用分页的时候，只会对主SQL进行分页查询，嵌套的查询不会被分页。对于嵌套查询不进行分页是正确的，所以这里是<b>支持的</b>。      
   
相关内容:[Mybatis关联结果查询分页方法][6]  

####**关联结果查询和关联嵌套查询的区别**
关联结果查询是查询出多个字段的数据，然后将字段拼接到相应的对象中，只会执行一次查询。  
关联嵌套查询是对每个嵌套的查询单独执行sql，会执行多次查询。  


###Mybatis-Sample项目 

这个项目是一个分页插件的测试项目，使用Maven构建，该项目目前提供了4种基本的使用方式，需要测试Mybatis分页插件的可以clone该项目，该项目中的PageHelper.java和Page<E>两个类不能保证随时和当前项目同步更新，使用时请注意！

项目地址：[http://git.oschina.net/free/Mybatis-Sample][7]

###v3.2.0 版本示例：
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
因为新增了一个Mybatis-Sample项目，所以这里的示例只是简短的一部分，需要更丰富的示例，请查看[Mybatis-Sample][7]项目

###对于两种分页方式如何选择   

1. 如果你不想在Mapper方法上增加一个带```RowBounds```参数的方法，并且你喜欢用Mapper接口形式调用，你可以使用```PageHelper.startPage```，并且该方法可以控制是否执行count方法。
2. 实际上在Mapper接口中添加一个带```RowBounds```参数的方法很容易，使用和不带```RowBounds```参数一样的xml就可以。
3. 如果你喜欢使用```sqlSession.selectList```这种命名空间方式的调用，使用```RowBounds```会更方便。

###关于MappedStatement  
```java
    MappedStatement qs = newMappedStatement(ms, new BoundSqlSqlSource(boundSql));
```
这段代码执行100万次耗时在1.5秒（测试机器：CPU酷睿双核T6600，4G内存）左右，因而不考虑对该对象进行缓存等考虑  

##更新日志   

###v3.2.0
1. 增加了对`Hsqldb`的支持，主要目的是为了方便测试使用`Hsqldb`
2. 增加了该项目的一个测试项目[Mybatis-Sample][7]，测试项目数据库使用`Hsqldb`

###v3.1.2
1. 解决count sql在oracle中的错误

###v3.1.1  
1. 统一返回值为```Page<E>```（可以直接按```List```使用）,方便在页面使用EL表达式，如```${page.pageNum}```,```${page.total}```   
   
###v3.1.0  
1. 解决了```RowBounds```分页的严重BUG，原先会在物理分页基础上进行内存分页导致严重错误，已修复
2. 增加对MySql的支持，该支持由[鲁家宁][9]增加。
  
###v3.0  
1. 现在支持两种形式的分页，使用```PageHelper.startPage```方法或者使用```RowBounds```参数  
2. ```PageHelper.startPage```方法修改，原先的```startPage(int pageNum, int pageSize)```默认求count，新增的```startPage(int pageNum, int pageSize, boolean count)```设置count=false可以不执行count查询  
3. 移除```endPage```方法，现在本地变量```localPage```改为取出后清空本地变量。
4. 修改```Page<E>```类，继承```ArrayList<E>```
5. 关于两种形式的调用，请看示例代码   
    
###v2.1    
1. 解决并发异常
2. 分页sql改为直接拼sql    

###v2.0  

1. 支持Mybatis缓存，count和分页同时支持（二者同步）  
2. 修改拦截器签名，拦截```Executor```
3. 将```Page<E>```类移到外面，方便调用

###v1.0  
1. 支持```<foreach>```等标签的分页查询
2. 提供便捷的使用方式

----------
###支持作者（支付宝二维码）<img src="https://tfsimg.alipay.com/images/mobilecodec/T1mShdXo4fXXXXXXXX" alt="Drawing" width="160px"/>扫码请慎重，谢谢支持！


  [1]: http://my.oschina.net/lujianing
  [2]: http://blog.csdn.net/column/details/mybatis-sample.html
  [3]: http://blog.csdn.net/column/details/mybatisqa.html
  [4]: http://my.oschina.net/flags/blog
  [5]: http://blog.csdn.net/isea533
  [6]: http://my.oschina.net/flags/blog/274000
  [7]: http://git.oschina.net/free/Mybatis-Sample
  [9]: http://my.oschina.net/lujianing