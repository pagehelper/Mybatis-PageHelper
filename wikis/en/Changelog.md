## Changelog

### 5.1.2 - 2017-09-18

- Solve the problem  when using the `PageHelper.orderBy` method alone #110;

### 5.1.1 - 2017-08-30

- The update to solve the problem and only SqlServer 2005,2008 related.
- Resolve `RegexWithNolockReplaceSql` in the Wrong regular `w?`, it should be `w+`.
- Resolved `SqlServerDialect` did not initialize the default `SimpleWithNolockReplaceSql` error.
- `SqlServerRowBoundsDialect` support for the replaceSql parameter.


### 5.1.0 - 2017-08-28

- Added the sorting functionality included in the previous version of 4.x, and the usage is consistent (PageHelper adds several sort-related methods).
- Paging SQL is converted to PreparedStatement SQL.
- Added `ReplaceSql` interface for handling sqlServer with (nolock) problem, add the replaceSql parameters, the optional value is `simple` and `regex`, or to achieve the `ReplaceSql` interface fully qualified class name. The default value is `simple`, still using the original way to deal with, the new regex will be convert `with (nolock)` to `table_PAGEWITHNOLOCK`.
- `PageRowBounds` add `count` attribute, you can control whether execute the count query.


### 5.0.4 - 2017-08-01

- Add a simple configuration support for the Phoenix database, You can configure `helperDialect=phoenix`. Can also automatically identify the Phoenix database jdbc url.
- Simplified cache of `msCountMap`
- Add `countSuffix` count query suffix configuration parameters, this parameter is configured for `PageInterceptor`, the default value is `_COUNT`.
- Add custom count query support, see below for details.

#### Add custom count query support

Add `countSuffix` count query suffix configuration parameters, this parameter is configured for `PageInterceptor`, the default value is `_COUNT`.

The paging plugin will preferentially find the handwritten paging query by the current query `msId + countSuffix`.

If there is no custom query, the query is still automatically created using the previous way.

For example, if there are two queries:
```xml
<select id="selectLeftjoin" resultType="com.github.pagehelper.model.Country">
    select a.id,b.countryname,a.countrycode from country a
    left join country b on a.id = b.id
    order by a.id
</select>
<select id="selectLeftjoin_COUNT" resultType="Long">
    select count(distinct a.id) from country a
    left join country b on a.id = b.id
</select>
```
The above `countSuffix` uses the default value of` _COUNT`, and the paging plugin will automatically get the query to `selectLeftjoin_COUNT`. This query needs to ensure that the result is correct.

The value of the return value must be `resultType =" Long "`, and the same parameter used by `selectLeftjoin` 'is used, so it is used in SQL to follow the selection of` selectLeftjoin`'.

Because the `selectLeftjoin_COUNT` method is invoked automatically, there is no need to provide the appropriate method on the interface, or if it is required to be invoked separately.

The above method to perform the portion of the output log is as follows：
```
DEBUG [main] - ==>  Preparing: select count(distinct a.id) from country a left join country b on a.id = b.id 
DEBUG [main] - ==> Parameters: 
TRACE [main] - <==    Columns: C1
TRACE [main] - <==        Row: 183
DEBUG [main] - <==      Total: 1
DEBUG [main] - Cache Hit Ratio [com.github.pagehelper.mapper.CountryMapper]: 0.0
DEBUG [main] - ==>  Preparing: select a.id,b.countryname,a.countrycode from country a left join country b on a.id = b.id order by a.id LIMIT 10 
DEBUG [main] - ==> Parameters: 
TRACE [main] - <==    Columns: ID, COUNTRYNAME, COUNTRYCODE
TRACE [main] - <==        Row: 1, Angola, AO
TRACE [main] - <==        Row: 2, Afghanistan, AF
TRACE [main] - <==        Row: 3, Albania, AL
```

### 5.0.3 -2017-06-20

- Solve the `supportMethodsArguments` parameter problem. It is recommended to upgrade to the latest version.

### 5.0.2 - 2017-05-30

- `Page<E>` implements `Closeable` interface, in JDK7+ which can use the in `try ()` call, it will automatically call `PageHelper.clearPage ();`[#58](https://github.com/pagehelper/Mybatis-PageHelper/issues/58)。
- fixed: DB2 paging must be specified sub-query alias, or an exception will occur [#52](https://github.com/pagehelper/Mybatis-PageHelper/issues/52)
- fixed：if `page.size() == 0` then `pageInfo.isIsLastPage()` is `false` [#50](https://github.com/pagehelper/Mybatis-PageHelper/issues/50)


### 5.0.1 - 2017-04-23
- Add the new parameter `countColumn` used to configure the automatic count column, the default value `0`, that is, `count(0).
- The `Page` class is also added with the `countColumn` parameter, which can be configured for a specific query.
- Modify the document display problem, by liumian* [PR #30](https://github.com/pagehelper/Mybatis-PageHelper/pull/30)
- Resolved sqlserver2012 paging error [42](https://github.com/pagehelper/Mybatis-PageHelper/issues/42)

### 5.0.0 - 2017-01-02

- Use Use [QueryInterceptor spec](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/src/main/java/com/github/pagehelper/QueryInterceptor.java) to handle paging logic
- New pagination plugin interceptor `com.github.pagehelper.PageInterceptor`
- New `Dialect` `PageHelper` is a special implementation class, the previous function is implemented in more user-friendly ways
- New pagination plugin only a `dialect` parameter, the default `dialect` is `PageHelper`
- `PageHelper` continue to support previously provided parameters, Among the latest to use the document has been fully updated
- `PageHelper` has a `helperDialect` parameter which is the same functional as the previous `dialect`
- Added paging implementation based on pure `RowBounds` and `PageRowBounds`, 
in `com.github. pagehelper. dialect. rowbounds` package, it is used as `dialect` Parameter sample implementation, more detailed documentation will be added later
- Removed inappropriate orderby functions that appear in pagination plugin. It will provide a separate sort plug-ins in the future
- Remove `PageHelper` are less commonly used methods
- A new document, an important part of the update has been mentioned in the changelog, provides the English version of this document
- fix bug [#149](http://git.oschina.net/free/Mybatis_PageHelper/issues/149)
- renamed Db2RowDialect to Db2RowBoundsDialect
- All thrown exceptions being replaced by PageException

## Older Changelogs are written in Chinese
You can [view here](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/Changelog.md)
