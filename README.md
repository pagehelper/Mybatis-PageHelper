#PageHelper说明  

###最新版为2.0  

如果你也在用Mybatis，建议尝试该分页插件，这个一定是<b>最方便</b>使用的分页插件。  

该插件目前只提供了Oracle的版本，具体介绍以及如何支持其他数据库，请看下面的介绍。  

注：如果真的有人写不出Mysql版本的，可以给我提Issues  

分页插件介绍：http://my.oschina.net/flags/blog/228699  

分页插件示例：http://my.oschina.net/flags/blog/228700  

##更新日志
###v2.0：  
1. 支持Mybatis缓存，count和分页同时支持（二者同步）  
2. 修改拦截器签名，拦截Executor，签名如下：    
	`@Intercepts(@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}))
`  
3. 将Page<E>类移到外面，方便调用  

###v1.0  
1. 支持foreach等标签的分页查询
2. 提供便捷的使用方式

 
