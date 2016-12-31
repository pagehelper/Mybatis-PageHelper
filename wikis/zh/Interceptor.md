## Executor 拦截器高级教程 - QueryInterceptor 规范
这篇文档涉及下面几个方面
1. Executor query 方法介绍
2. 拦截器配置和调用顺序
3. 拦截 query 方法的技巧
4. 拦截 query 方法的规范
5. 如何配置不同的 Executor 插件

### 1. Executor query 方法介绍
在 MyBatis 的拦截器的文档部分，我们知道 Executor 中的 query 方法可以被拦截，如果你真正写过这个方法的拦截器，
你可能会知道在 Executor 中的 query 方法有两个：
```java
<E> List<E> query(
      MappedStatement ms, 
      Object parameter, 
      RowBounds rowBounds, 
      ResultHandler resultHandler, 
      CacheKey cacheKey, 
      BoundSql boundSql) throws SQLException;

<E> List<E> query(
      MappedStatement ms, 
      Object parameter, 
      RowBounds rowBounds, 
      ResultHandler resultHandler) throws SQLException;
```
这两个方法的区别是第一个方法多两个参数 CacheKey 和 BoundSql，在多数情况下，我们用拦截器的目的就是针对 SQL
做处理，如果能够拦截第一个方法，可以直接得到 BoundSql 对象，就会很容易的得到执行的 SQL，也可以对 SQL
做处理。

虽然想的很好，但是 MyBatis 提供的 Exctutor 实现中，参数多的这个 query
方法都是被少的这个 query 方法在内部进行调用的。

在 `CachingExecutor` 中：
```java
public <E> List<E> query(
        MappedStatement ms, 
        Object parameter, 
        RowBounds rowBounds, 
        ResultHandler resultHandler) throws SQLException {
    BoundSql boundSql = ms.getBoundSql(parameterObject);
    CacheKey key = createCacheKey(ms, parameterObject, rowBounds, boundSql);
    return query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
}
```
在 `BaseExecutor` 中：
```java
public <E> List<E> query(
        MappedStatement ms, 
        Object parameter, 
        RowBounds rowBounds, 
        ResultHandler resultHandler) throws SQLException {
    BoundSql boundSql = ms.getBoundSql(parameter);
    CacheKey key = createCacheKey(ms, parameter, rowBounds, boundSql);
    return query(ms, parameter, rowBounds, resultHandler, key, boundSql);
}
```
上面这两个方法一样。由于第一个 query 方法在这里是内部调用，并且我们所有的拦截器都是层层代理的 `CachingExecutor` 或基于 `BaseExecutor` 的实现类，所以我们能拦截的就是参数少的这个方法。

分页插件开始从 Executor 拦截开始就一直是拦截的参数少的这个方法。但是从 5.0 版本开始，query 的这两个方法都可以被拦截了。在讲这个**原理**之前，我们先了解一下拦截器的执行顺序。

### 2. 拦截器配置和调用顺序
拦截器的调用顺序分为两大种，第一种是拦截的不同对象，例如拦截 Executor 和 拦截 StatementHandler 就属于不同的拦截对象，
这两类的拦截器在整体执行的逻辑上是不同的，在 Executor 中的 query 方法执行过程中，会调用下面的代码：
```java
public <E> List<E> doQuery(
        MappedStatement ms, 
        Object parameter, 
        RowBounds rowBounds, 
        ResultHandler resultHandler, 
        BoundSql boundSql) throws SQLException {
    Statement stmt = null;
    try {
          Configuration configuration = ms.getConfiguration();
          StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, resultHandler, boundSql);
          stmt = prepareStatement(handler, ms.getStatementLog());
          return handler.<E>query(stmt, resultHandler);
    } finally {
        closeStatement(stmt);
    }
}
```
在这段代码中，才会轮到 StatementHandler 去执行，StatementHandler 属于 Executor 执行过程中的一个子过程。
所以这两种不同类别的插件在配置时，一定是先执行 Executor 的拦截器，然后才会轮到 StatementHandler。所以这种情况下配置拦截器的顺序就不重要了，在 MyBatis 逻辑上就已经控制了先后顺序。

第二种拦截器的顺序就是指拦截同一种对象的同一个方法，例如都拦截 Executor 的 query 方法，这时你配置拦截器的顺序就会对这里有影响了。假设有如下几个拦截器，都是拦截的 Executor 的 query 方法。
```
<plugins>
    <plugin interceptor="com.github.pagehelper.ExecutorQueryInterceptor1"/>
    <plugin interceptor="com.github.pagehelper.ExecutorQueryInterceptor2"/>
    <plugin interceptor="com.github.pagehelper.ExecutorQueryInterceptor3"/>
</plugins>
```
在`org.apache.ibatis.session.Configuration` 中有如下方法：
```java
public void addInterceptor(Interceptor interceptor) {
    interceptorChain.addInterceptor(interceptor);
}
```
MyBatis 会按照拦截器配置的顺序依次添加到 interceptorChain 中，其内部就是 `List<Interceptor> interceptors`。再看 `Configuration` 中创建 Executor 的代码：
```java
public Executor newExecutor(Transaction transaction, ExecutorType executorType) {
    executorType = executorType == null ? defaultExecutorType : executorType;
    executorType = executorType == null ? ExecutorType.SIMPLE : executorType;
    Executor executor;
    if (ExecutorType.BATCH == executorType) {
        executor = new BatchExecutor(this, transaction);
    } else if (ExecutorType.REUSE == executorType) {
        executor = new ReuseExecutor(this, transaction);
    } else {
        executor = new SimpleExecutor(this, transaction);
    }
    if (cacheEnabled) {
        executor = new CachingExecutor(executor);
    }
    executor = (Executor) interceptorChain.pluginAll(executor);
    return executor;
}
```
在调用 interceptorChain.pluginAll 之前，executor 就是前一节中的 `CachingExecutor` 或基于 `BaseExecutor` 的实现类。
然后看 interceptorChain.pluginAll 方法：
```java
public Object pluginAll(Object target) {
    for (Interceptor interceptor : interceptors) {
        target = interceptor.plugin(target);
    }
    return target;
}
```
前面我们配置拦截器的顺序是1，2，3。在这里也会按照 1，2，3 的顺序被层层代理，代理后的结构如下：
```json
Interceptor3:{
    Interceptor2: {
        Interceptor1: {
            target: Executor
        }
    }
}
```
从这个结构应该就很容易能看出来，将来执行的时候肯定是按照 3>2>1>Executor>1>2>3 的顺序去执行的。
可能有些人不知道为什么3>2>1>Executor之后会有1>2>3，这是因为使用代理时，调用完代理方法后，还能继续进行其他处理。处理结束后，将代理方法的返回值继续往外返回即可。例如：
```    
Interceptor3 前置处理      
Object result = Interceptor2..query(4个参数方法);     
Interceptor3 后续处理   
return result;
```
对于 Interceptor2.query 方法也是相同的逻辑：
```
Interceptor2 前置处理      
Object result = Interceptor1..query(4个参数方法);     
Interceptor2 后续处理   
return result;
```
同理 Interceptor1.query ：
```
Interceptor1 前置处理      
Object result = executor.query(4个参数方法);     
Interceptor1 后续处理   
return result;
```
叠加到一起后，如下：
```
Interceptor3 前置处理
Interceptor2 前置处理
Interceptor1 前置处理  
Object result = executor.query(4个参数方法);     
Interceptor1 后续处理   
Interceptor2 后续处理  
Interceptor3 后续处理   
return result;
```
所以这个顺序就是 3>2>1>Executor>1>2>3。

在你弄清楚这个逻辑后，再继续往下看，因为后面的技巧会颠覆这个逻辑，所以才会有后面的规范以及如何配置不同的插件。

### 3. 拦截 query 方法的技巧
上一节的内容中，对拦截器的用法是最常见的一种用法，所以才会出现这种都能理解的执行顺序。但是分页插件 5.0 不是这样，这个插件颠覆了这种顺序，这种颠覆其实也很普通，这也是本节要说的技巧。

在我写作 MyBatis 技术书籍的过程中（还没写完，已经因为分页插件占用了几周的写作时间），我就在考虑为什么不能拦截第一个 query（6个参数的）方法，
如果能拦截这个方法，就可以直接拿到 BoundSql，然后处理 SQL 就很容易实现其他的操作。

在第 1 节介绍为什么第一个 query 方法不能被拦截时，是因为下面这段代码：
```java
public <E> List<E> query(
        MappedStatement ms, 
        Object parameter, 
        RowBounds rowBounds, 
        ResultHandler resultHandler) throws SQLException {
    BoundSql boundSql = ms.getBoundSql(parameter);
    CacheKey key = createCacheKey(ms, parameter, rowBounds, boundSql);
    return query(ms, parameter, rowBounds, resultHandler, key, boundSql);
}
```
既然 `CachingExecutor` 或基于 `BaseExecutor` 的实现类只是这么简单的调用两个方法得到了 BoundSql 和 Cachekey，我们为什么不直接替代他们呢？

所以我们可以有类似下面的拦截器用法：
```java
@Intercepts(@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}))
public class QueryInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameterObject = args[1];
        RowBounds rowBounds = (RowBounds) args[2];
        ResultHandler resultHandler = (ResultHandler) args[3];
        Executor executor = (Executor) invocation.getTarget();
        BoundSql boundSql = ms.getBoundSql(parameterObject);
        //可以对参数做各种处理
        CacheKey cacheKey = executor.createCacheKey(ms, parameterObject, rowBounds, boundSql);
        return executor.query(ms, parameterObject, rowBounds, resultHandler, cacheKey, boundSql);
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }

}
```
这个拦截器直接替代了原有 Executor 的部分逻辑，直接去调用了 6 个参数的方法，因而导致 4 个参数的后续方法被跳过了。但是由于这里的 executor 是代理对象
所以 6 个参数的 query 方法可以被代理了，这就扰乱了上一节中的执行顺序。

在上一节拦截器的例子中，做简单修改，将 ExecutorQueryInterceptor2 换成上面的 QueryInterceptor，配置如下：
```
<plugins>
    <plugin interceptor="com.github.pagehelper.ExecutorQueryInterceptor1"/>
    <plugin interceptor="com.github.pagehelper.QueryInterceptor"/>
    <plugin interceptor="com.github.pagehelper.ExecutorQueryInterceptor3"/>
</plugins>
```
代理后的结构如下：
```json
Interceptor3:{
    QueryInterceptor: {
        Interceptor1: {
            target: Executor
        }
    }
}
```
这时，调用顺序就变了，Interceptor3 执行顺序如下：
```    
Interceptor3 前置处理      
Object result = QueryInterceptor.query(4个参数方法);     
Interceptor3 后续处理   
return result;
```
QueryInterceptor.query 执行逻辑如下：
```
Interceptor2 前置处理      
Object result = executor.query(6个参数方法);     
Interceptor2 后续处理   
return result;
```
在 QueryInterceptor 中，没有继续执行 4个参数方法，而是执行了 6 个参数方法。
但是 Interceptor1 拦截的 4 个参数的方法，所以 Interceptor1 就被跳过去了，整体的执行逻辑就变成下面这样了：
```
Interceptor3 前置处理
Interceptor2 前置处理
Object result = executor.query(6个参数方法);     
Interceptor2 后续处理  
Interceptor3 后续处理   
return result;
```
如果 Interceptor1 拦截的是 6 个参数的方法，因为 QueryInterceptor 获取的是 Interceptor1 代理的 executor 对象，那么 Interceptor1 就会被 QueryInterceptor 继续执行下去。

分页插件就是类似 QueryInterceptor 的执行逻辑，所以当你使用 5.0 版本之后的插件时，如果你还需要配置其他 Executor 的 query 插件，你就会遇到一些问题（可以解决，继续往下看）。

如果你是自己开发的插件，那么你按照下一节的规范去开发也不会遇到问题。如果你使用的其他人提供的插件，按照第 5 节的配置顺序也能解决问题。

### 4. 拦截 query 方法的规范

QueryInterceptor 的逻辑就是进去的是 4 个参数的方法，出去的是 6 个参数的方法。这种处理方法不仅仅不方便和一般的 Excutor 拦截器搭配使用，
当出现两个以上类似 QueryInterceptor 的插件时，由于接口变了，类似 QueryInterceptor 插件也无法连贯的执行下去。
因而有必要解决这个问题。解决的办法就是使用统一的规范。经过规范后 QueryInterceptor 如下：
```java
@Intercepts(
    {
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
    }
)
public class QueryInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args[1];
        RowBounds rowBounds = (RowBounds) args[2];
        ResultHandler resultHandler = (ResultHandler) args[3];
        Executor executor = (Executor) invocation.getTarget();
        CacheKey cacheKey;
        BoundSql boundSql;
        //由于逻辑关系，只会进入一次
        if(args.length == 4){
            //4 个参数时
            boundSql = ms.getBoundSql(parameter);
            cacheKey = executor.createCacheKey(ms, parameter, rowBounds, boundSql);
        } else {
            //6 个参数时
            cacheKey = (CacheKey) args[4];
            boundSql = (BoundSql) args[5];
        }
        //TODO 自己要进行的各种处理
        //注：下面的方法可以根据自己的逻辑调用多次，在分页插件中，count 和 page 各调用了一次
        return executor.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }

}
```
注意两个变化，第一个就是拦截器签名同时拦截了 4 个 和 6 个参数的方法，这样不管那个插件在前在后都会被执行。

第二个变化就是这段代码：
```java
CacheKey cacheKey;
BoundSql boundSql;
//由于逻辑关系，只会进入一次
if(args.length == 4){
    //4 个参数时
    boundSql = ms.getBoundSql(parameterObject);
    cacheKey = executor.createCacheKey(ms, parameterObject, rowBounds, boundSql);
} else {
    //6 个参数时
    cacheKey = (CacheKey) args[4];
    boundSql = (BoundSql) args[5];
}
```
如果这个插件配置的靠后，是通过 4 个参数方法进来的，我们就获取这两个对象。如果这个插件配置的靠前，已经被别的拦截器处理成 6 个参数的方法了，
那么我们直接从 args 中取出这两个参数直接使用即可。取出这两个参数就保证了当其他拦截器对这两个参数做过处理时，这两个参数在这里会继续生效。

假设有个排序插件和分页插件，排序插件将 BoundSql 修改为带排序的 SQL 后，SQL 会继续交给分页插件使用。分页插件的分页 SQL 执行时，会保留排序去执行，
这样的规范就保证了两个插件都能正常的执行下去。

所以如果大家想要使用这种方式去实现拦截器，建议大家遵守这个规范。

这个规范对于已经存在的插件来说就没法控制了，但是仍然可以通过配置顺序来解决。

### 5. 如何配置不同的 Executor 插件

当引入类似 QueryInterceptor 插件时，由于扰乱了原有的插件执行方式，当配置 Executor 顺序不对时会导致插件无法生效。

第 4 节中的例子：
```
<plugins>
    <plugin interceptor="com.github.pagehelper.ExecutorQueryInterceptor1"/>
    <plugin interceptor="com.github.pagehelper.QueryInterceptor"/>
    <plugin interceptor="com.github.pagehelper.ExecutorQueryInterceptor3"/>
</plugins>
```
首先执行顺序为  3>Query>1>Executor，由于 Query 是 4 或 6 个参数进来，6 个参数出去。
所以在 Query 前面执行的拦截器必须是 4 个的（Query 规范拦截器先后都能执行，需要根据逻辑配置先后）参数的，在 Query 后面执行的拦截器必须是 6 个参数的。

这个顺序对应到配置顺序时，也就是 4 个参数的配置在 QueryInterceptor 拦截器的下面，6 个参数的配置在 QueryInterceptor 拦截器的上面。
按照这个顺序进行配置时，就能保证拦截器都执行。

如果你想获得如分页插件（QueryInterceptor 规范）执行的 SQL，你就得按照 QueryInterceptor 规范去实现，否则只能配置在分页插件的下面，也就只能获得分页处理前的 SQL。