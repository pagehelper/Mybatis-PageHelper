## 项目测试   

为了保证分页插件的稳定性，项目中包含大量的单元测试，并且可以针对支持的数据库进行测试。

### 分页插件多数据库测试  

为了更方便的测试不同的数据库，在`src/test/resources`目录下增加了不同数据库的mybatis配置文件，通过修改`test.properties`中的配置可以让测试使用不同的配置进行测试。  

`test.properties`内容：  

```properties
#首先需要在本机配置对应的数据库

#想要测试那个数据库，这里就写那个数据库
#这个值和test/resources中的数据库对应的文件夹名字相同
#目前可选为:
#hsqldb
#mysql
#mariadb - 注意测试中的端口是3309(因为默认和mysql是一样的)
#oracle
#postgresql
#sqlserver
#db2
#h2
#derby
database = hsqldb
```  
各种数据库对应的sql文件都在对应的目录中。