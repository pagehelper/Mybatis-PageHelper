---
name: PageHelper issue report template
about: If you would like to report a issue to PageHelper, please use this template.

---

- [ ] 我已在 [issues](https://github.com/pagehelper/Mybatis-PageHelper/issues) 搜索类似问题，并且不存在相同的问题.

## 异常模板

### 使用环境

* PageHelper 版本: xxx
* 数据库类型和版本: xxx
* JDBC_URL: xxx

### SQL 解析错误

#### 分页参数

```java
PageHelper.startPage(1, 10);
xxMapper.select(model);
```

#### 原 SQL

```sql
select * from xxx where xxx = xxx
```

#### 期望的结果：

```sql
select * from xxx where xxx = xxx limit 10
```

### 完整异常信息

```
异常信息放在这里
```

### 其他类型的错误

## 功能建议

详细说明，尽可能提供(伪)代码示例。
