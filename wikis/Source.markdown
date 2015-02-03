#源码说明

分页插件项目中的正式代码一共有个6个Java文件，这6个文件的说明如下：  

 - `Page<E>`\[必须\]：分页参数类，该类继承`ArrayList`，虽然分页查询返回的结果实际类型是`Page<E>`,但是可以完全不出现所有的代码中，可以直接当成`List`使用。返回值不建议使用`Page`，建议仍然用`List`。如果需要用到分页信息，使用下面的`PageInfo`类对List进行包装即可。  
 
 - `PageHelper`\[必须\]：分页插件拦截器类，对Mybatis的拦截在这个类中实现。
 
 - `PageInfo`\[可选\]：`Page<E>`的包装类，包含了全面的分页属性信息。  
 
 - `SqlParser`\[必须\]：提供高效的count查询sql。主要是智能替换原sql语句为count(*)，去除不带参数的order by语句。需要`jsqlparser-0.9.1.jar`支持。  
 
 - `SqlUtil`\[必须\]：分页插件工具类，分页插件逻辑类，分页插件的主要实现方法都在这个类中。 

 - `SqlServer`\[必须\]：sqlserver分页工具，该类独立，依赖jsqlparser，3.6.0版本添加。 