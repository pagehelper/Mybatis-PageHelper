##项目测试   

为了保证分页插件的稳定性，项目中包含大量的单元测试，并且可以针对支持的数据库进行测试。

###分页插件多数据库测试  

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


###`SqlUtil.testSql`测试sql方法  

为了便于测试sql语句方面的问题，提供了`SqlUtil.testSql`方法，使用方法如下：  

```java
String originalSql = "Select * from sys_user o where abc = ? order by id desc , name asc";
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

##Mybatis-Sample项目 

这个项目是一个分页插件的WEB测试项目，使用Maven构建，只包含一个简单的例子和简单的页面分页效果。

项目地址：[http://git.oschina.net/free/Mybatis-Sample](http://git.oschina.net/free/Mybatis-Sample)
