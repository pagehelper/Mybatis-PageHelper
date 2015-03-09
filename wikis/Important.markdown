##重要提示  

###`PageHelper.startPage`方法重要提示

只有紧跟在`PageHelper.startPage`方法后的<b>第一个</b>Mybatis的<b>查询（Select方法）</b>方法会被分页。

<br/>

###请不要配置多个分页插件

请不要在系统中配置多个分页插件(使用Spring时,`mybatis-config.xml`和`Spring<bean>`配置方式，请选择其中一种，不要同时配置多个分页插件)！

<br/>

###分页插件不支持带有`for update`语句的分页

对于带有`for update`的sql，会抛出运行时异常，对于这样的sql建议手动分页，毕竟这样的sql需要重视。

<br/>

###分页插件不支持关联的嵌套结果

原因以及解决方法可以看这里：
>http://my.oschina.net/flags/blog/274000 

分支插件不支持关联的嵌套结果，但是支持关联的嵌套查询。只会对主sql进行分页，嵌套的sql不会被分页。  

有关关联的相关介绍可以看官方文档：http://mybatis.github.io/mybatis-3/zh/sqlmap-xml.html

<br/>