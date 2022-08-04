package com.github.pagehelper.config;

/**
 * 配置项枚举
 *
 * @author majiang
 * @version 6.0.0
 */
public enum PageHelperConfigEnum {

    /**
     * 默认值为 false，该参数对使用 RowBounds 作为分页参数时有效。
     * 当该参数设置为 true 时，会将 RowBounds 中的 offset 参数当成 pageNum 使用，可以用页码和页面大小两个参数进行分页。
     */
    offsetAsPageNum,
    /**
     * 默认值为false，该参数对使用 RowBounds 作为分页参数时有效。
     * 当该参数设置为true时，使用 RowBounds 分页会进行 count 查询。
     */
    rowBoundsWithCount,
    /**
     * 默认值为 false，当该参数设置为 true 时，
     * 如果 pageSize=0 或者 RowBounds.limit = 0 就会查询出全部的结果（相当于没有执行分页查询，但是返回结果仍然是 Page 类型）。
     */
    pageSizeZero,
    /**
     * 分页合理化参数，默认值为false。
     * 当该参数设置为 true 时，pageNum<=0 时会查询第一页。
     * pageNum>pages（超过总数时），会查询最后一页。
     * 默认false 时，直接根据参数进行查询。
     */
    reasonable,
    /**
     * 为了支持startPage(Object params)方法，增加了该参数来配置参数映射，
     * 用于从对象中根据属性名取值， 可以配置 pageNum,pageSize,count,pageSizeZero,reasonable，
     * 不配置映射的用默认值， 默认值为
     * pageNum=pageNum;pageSize=pageSize;count=countSql;reasonable=reasonable;pageSizeZero=pageSizeZero
     */
    params,
    /**
     * 支持通过 Mapper 接口参数来传递分页参数，默认值false，
     * 分页插件会从查询方法的参数值中，自动根据上面 params 配置的字段中取值，查找到合适的值时就会自动分页。
     * 使用方法可以参考测试代码中的 com.github.pagehelper.test.basic 包下的 ArgumentsMapTest 和 ArgumentsObjTest。
     */
    supportMethodsArguments,
    /**
     * 默认值为 true。
     * 当使用运行时动态数据源或没有设置 helperDialect 属性自动获取数据库类型时，
     * 会自动获取一个数据库连接， 通过该属性来设置是否关闭获取的这个连接，默认true关闭，
     * 设置为 false 后，不会关闭获取的连接，这个参数的设置要根据自己选择的数据源来决定。
     */
    closeConn,
    /**
     * 参数，用于控制默认不带 count 查询的方法中，是否执行 count 查询，
     * 默认 true 会执行 count 查询，这是一个全局生效的参数，多数据源时也是统一的行为。
     */
    defaultCount,
    /**
     * 默认情况下会使用 PageHelper 方式进行分页，如果想要实现自己的分页逻辑，
     * 可以实现 Dialect(com.github.pagehelper.Dialect) 接口，然后配置该属性为实现类的全限定名称。
     */
    dialect,
    /**
     * 如果你使用的数据库不在这个列表时，你可以配置 dialectAlias 参数。
     * 这个参数允许配置自定义实现的别名，可以用于根据 JDBCURL 自动获取对应实现，允许通过此种方式覆盖已有的实现，配置示例如（多个配置时使用分号隔开）：
     * dialectAlias=oracle=com.github.pagehelper.dialect.helper.OracleDialect
     */
    dialectAlias,
    /**
     * 分页插件会自动检测当前的数据库链接，自动选择合适的分页方式。 你可以配置helperDialect属性来指定分页插件使用哪种方言。配置时，可以使用下面的缩写值：
     * oracle,mysql,mariadb,sqlite,hsqldb,postgresql,db2,sqlserver,informix,h2,sqlserver2012,derby
     * 特别注意：使用 SqlServer2012 数据库时，需要手动指定为 sqlserver2012，否则会使用 SqlServer2005 的方式进行分页。
     * 你也可以实现 AbstractHelperDialect，然后配置该属性为实现类的全限定名称即可使用自定义的实现方法。
     */
    helperDialect,
    /**
     * 默认值为 false。
     * 设置为 true 时，允许在运行时根据多数据源自动识别对应方言的分页 （不支持自动选择sqlserver2012，只能使用sqlserver），
     * 用法和注意事项参考下面的场景五。
     */
    autoRuntimeDialect,
    /**
     * `AutoDialect` 接口用于自动获取数据库类型，可以通过 `autoDialectClass` 配置为自己的实现类，默认使用 `DataSourceNegotiationAutoDialect`，优先根据连接池获取。
     * 默认实现中，增加针对 `hikari,druid,tomcat-jdbc,c3p0,dbcp` 类型数据库连接池的特殊处理，直接从配置获取jdbcUrl，当使用其他类型数据源时，仍然使用旧的方式获取连接在读取jdbcUrl。
     * 想要使用和旧版本完全相同方式时，可以配置 `autoDialectClass=old`。当数据库连接池类型非常明确时，建议配置为具体值，例如使用 hikari 时，配置 `autoDialectClass=hikari`，使用其他连接池时，配置为自己的实现类。
     */
    autoDialectClass,
    /**
     * 是否展示启动banner
     */
    banner
}
