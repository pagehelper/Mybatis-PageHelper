## 重要提示  

### `PageHelper.startPage`方法重要提示

只有紧跟在`PageHelper.startPage`方法后的<b>第一个</b>Mybatis的<b>查询（Select）</b>方法会被分页。

### 请不要配置多个分页插件

请不要在系统中配置多个分页插件(使用Spring时,`mybatis-config.xml`和`Spring<bean>`配置方式，请选择其中一种，不要同时配置多个分页插件)！

### 分页插件不支持带有`for update`语句的分页

对于带有`for update`的sql，会抛出运行时异常，对于这样的sql建议手动分页，毕竟这样的sql需要重视。

### 分页插件不支持嵌套结果映射

由于嵌套结果方式会导致结果集被折叠，因此分页查询的结果在折叠后总数会减少，所以无法保证分页结果数量正确。