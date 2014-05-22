#PageHelper说明  

###最新版为3.0_beta版  

如果你也在用Mybatis，建议尝试该分页插件，这个一定是<b>最方便</b>使用的分页插件。  

该插件目前只提供了Oracle的版本，具体介绍以及如何支持其他数据库，请看下面的介绍。  

**注**：欢迎各位提供其他数据库版本的分页插件  

Mybatis项目：http://mybatis.github.io/mybatis-3/zh/index.html

Mybatis文档：http://mybatis.github.io/mybatis-3/zh/index.html

###使用方法
在Mybatis的配置xml中配置拦截器插件:    
```xml
<plugins>
	<plugin interceptor="PageHelper"></plugin>
</plugins>
```   
这里的PageHelper要使用完整的类路径，需要加上包路径。


###不支持的情况   

对于**关联结果查询**，使用分页得不到正常的结果，因为只有把数据全部查询出来，才能得到最终的结果，对这个结果进行分页才有效。因而如果是这种情况，必然要先全部查询，在对结果处理，这样就体现不出分页的作用了。
对于**关联嵌套查询**，使用分页的时候，只会对主SQL进行分页查询，嵌套的查询不会被分页。  
   
####**关联结果查询和关联嵌套查询的区别**
关联结果查询是查询出多个字段的数据，然后将字段拼接到相应的对象中，只会执行一次查询。  
关联嵌套查询是对每个嵌套的查询单独执行sql，会执行多次查询。

###v3.0版本示例：
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
    //不进行count查询时，返回结果就是List<SysLoginLog>类型
    List<SysLoginLog> logs = sysLoginLogMapper
            .findSysLoginLog(logip, username, loginDate, exitDate, logerr);
    Assert.assertEquals(10, logs.size());

    //当第三个参数没有或者为true的时候，进行count查询
    PageHelper.startPage(2, 10);
    //返回结果默认是List<SysLoginLog>
    //可以通过强制转换为Page<SysLoginLog>,该对象除了包含返回结果外，还包含了分页信息
    Page<SysLoginLog> page = (Page<SysLoginLog>) sysLoginLogMapper
            .findSysLoginLog(logip, username, loginDate, exitDate, logerr);
    Assert.assertEquals(10, page.getResult().size());
    //进行count查询，返回结果total>0
    Assert.assertTrue(page.getTotal() > 0);
}

@Test
public void testPageHelperByRowbounds() throws Exception {
    String logip = "";
    String username = "super";
    String loginDate = "";
    String exitDate = null;
    String logerr = null;
    //使用RowBounds方式，不需要PageHelper.startPage
    //RowBounds方式默认不进行count查询，返回结果默认为List<SysLoginLog>
    //可以通过强制转换为Page<SysLoginLog>，在不进行count查询的情况，没必要强转
    List<SysLoginLog> logs = sysLoginLogMapper
            .findSysLoginLog(logip, username, loginDate, exitDate, logerr, new RowBounds(0, 10));
    Assert.assertEquals(10, logs.size());
    //这里进行了强制转换，实际上并没有必要
    Page<SysLoginLog> logs2 = (Page<SysLoginLog>) sysLoginLogMapper
            .findSysLoginLog(logip, username, loginDate, exitDate, logerr, new RowBounds(0, 10));
    Assert.assertEquals(10, logs2.size());
}

@Test
public void testPageHelperByNamespaceAndRowBounds() throws Exception {
    //没有RowBounds不进行分页
    List<SysLoginLog> logs = sqlSession.selectList("findSysLoginLog2");
    Assert.assertNotEquals(10, logs.size());
    
    //使用RowBounds分页
    List<SysLoginLog> logs2 = sqlSession
            .selectList("findSysLoginLog2",null,new RowBounds(0,10));
    Assert.assertEquals(10, logs2.size());
}
```
###示例的Mapper接口:  
```java
        /**
     * 根据查询条件查询登录日志
     * @param logip
     * @param username
     * @param loginDate
     * @param exitDate
     * @return
     */
    List<SysLoginLog> findSysLoginLog(@Param("logip") String logip,
                                      @Param("username") String username,
                                      @Param("loginDate") String loginDate,
                                      @Param("exitDate") String exitDate,
                                      @Param("logerr") String logerr);
    /**
     * 根据查询条件查询登录日志
     * @param logip
     * @param username
     * @param loginDate
     * @param exitDate
     * @return
     */
    List<SysLoginLog> findSysLoginLog(@Param("logip") String logip,
                                      @Param("username") String username,
                                      @Param("loginDate") String loginDate,
                                      @Param("exitDate") String exitDate,
                                      @Param("logerr") String logerr,
                                      RowBounds rowBounds);
```
    
###示例Mapper接口对应的xml,两个接口方法对应同一个配置:    
```xml
    <select id="findSysLoginLog" resultType="SysLoginLog">
        select * from sys_login_log a
        <if test="username != null and username != ''">
            left join sys_user b on (a.userid = b.username or a.userid = b.userid)
        </if>
        <where>
            <if test="logip!=null and logip != ''">
                a.logip like '%'||#{logip}||'%'
            </if>
            <if test="username != null and username != ''">
                and (b.username like '%'||#{username}||'%' or b.realname like '%'||#{username}||'%')
            </if>
            <if test="loginDate!=null and loginDate!=''">
                and to_date(substr(a.logindate,0,10),'yyyy-MM-dd') = to_date(#{loginDate},'yyyy-MM-dd')
            </if>
            <if test="exitDate!=null and exitDate!=''">
                and to_date(substr(a.EXITDATE,0,10),'yyyy-MM-dd') = to_date(#{exitDate},'yyyy-MM-dd')
            </if>
            <if test="logerr!=null and logerr!=''">
                and a.logerr like '%'||#{logerr}||'%'
            </if>
        </where>
        order by logid desc
    </select>
    
    <!-- namespace调用的方法 -->
    <select id="findSysLoginLog2" resultType="SysLoginLog">
        select * from sys_login_log order by logid desc
    </select>
```
###关于MappedStatement  
```java
    MappedStatement qs = newMappedStatement(ms, new BoundSqlSqlSource(boundSql));
```
这段代码执行100万次耗时在1.5秒（测试机器：CPU酷睿双核T6600，4G内存）左右，因而不考虑对该对象进行缓存等考虑
##更新日志   
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
2. 修改拦截器签名，拦截Executor
3. 将Page<E>类移到外面，方便调用

###v1.0  
1. 支持foreach等标签的分页查询
2. 提供便捷的使用方式

 
