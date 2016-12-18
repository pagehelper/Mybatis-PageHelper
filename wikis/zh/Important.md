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

###`reasonable`参数说明

由于许多人直接复制文档中的配置，没有仔细看该参数的含义，导致查询莫名其妙。

如果你配置了该参数为`true`，那么如果你的数据一共有12条，当你查询`PageHelper.startPage(7,5)`的时候。

以`mysql`为例，你认为应该是`limit 35,5`，实际上是`limit 10,5`，为什么会这样呢？

因为你只有12条数据，每页5条的时候不可能存在第7页，只有3页，所以参数会自动变为`startPage(3,5)`，查询最后一页的结果。

所以如果你使用了合理化，你就要知道为什么会这样，否则就不要配置`reasonable`（默认`false`）。

<br/>

###分页插件不支持关联的嵌套结果

原因以及解决方法可以看这里：
>http://my.oschina.net/flags/blog/274000 

分支插件不支持关联的嵌套结果，但是支持关联的嵌套查询。只会对主sql进行分页，嵌套的sql不会被分页。  

有关关联的相关介绍可以看官方文档：http://mybatis.github.io/mybatis-3/zh/sqlmap-xml.html

<br/>