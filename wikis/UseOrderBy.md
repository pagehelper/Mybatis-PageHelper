#如何使用4.0.0版本提供的排序插件

前提是你已经按照分页插件的文档配置好了分页插件。

##`PageHelper.orderBy("xxx")`方法

该方法可以独立使用，使用后紧跟的第一个查询语句会增加指定的排序，使用起来很方便。

你唯一需要注意的地方是，你`orderBy`指定的列是有效的，否则SQL会报错。

使用例子：

```java
//正常情况下，查询结果第一个id是1
List<OrderCountry> list = orderMapper.selectAll();
Assert.assertEquals(1, (int) list.get(0).getId());

//倒序排列后第一个id是183
PageHelper.orderBy("id desc");
list = orderMapper.selectAll();
Assert.assertEquals(183, (int) list.get(0).getId());

//根据countryname倒序后，第一个id成了181
PageHelper.orderBy("countryname desc");
list = orderMapper.selectAll();
Assert.assertEquals(181, (int) list.get(0).getId());
```

`selectAll`方法对应的xml：

```xml
<select id="selectAll" resultType="com.github.orderbyhelper.mapper.OrderCountry">
    select * from country_order order by id
</select>
```

以上三个查询输出的SQL如下：

```sql
select * from country_order order by id

SELECT * FROM country_order order by id desc

SELECT * FROM country_order order by countryname desc
```

##`PageHelper.orderBy("xxx")`方法配合`PageHelper.startPage(xxx)`

直接看例子：

```java
PageHelper.startPage(1, 10);
PageHelper.orderBy("countryname desc");
list = orderMapper.selectAll();
Assert.assertEquals(181, (int) list.get(0).getId());
```

输出的SQL(count查询和分页):

```sql
SELECT count(*) FROM country_order

SELECT * FROM country_order order by countryname desc limit ? offset ?
```

这种情况使用的时候，下面这两个语句没有先后顺序，都会对查询产生影响

```java
PageHelper.startPage(1, 10);
PageHelper.orderBy("countryname desc");
```

##`PageHelper.startPage(1, 10, "id desc")`方法

这个方法可以说是对上面一种情况的结合，效果是一样的

例子：

```java
PageHelper.startPage(1, 10, "id desc");
list = orderMapper.selectAll();
Assert.assertEquals(183, (int) list.get(0).getId());
```

##重点说明

这里一定要注意一点，那就是分页和排序都使用`ThreadLocal`实现的，并且两者的`ThreadLocal`都在分页插件拦截器中清空的。

所以每次执行完一个查询后，分页和排序的`ThreadLocal`就会自动清空，清空就对后续的查询不会产生任何影响。