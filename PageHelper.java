import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.TextSqlNode;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 * Mybatis - 通用分页拦截器v2.0
 *
 * @author liuzh/abel533/isea
 *         Created by liuzh on 14-4-15.
 *         Update by liuzh on 14-5-20.
 */
@Intercepts(@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}))
public class PageHelper implements Interceptor {

    private static final ThreadLocal<Page> localPage = new ThreadLocal<Page>();

    private static final List<ResultMapping> EMPTY_RESULTMAPPING = new ArrayList<ResultMapping>(0);

    /**
     * 开始分页
     *
     * @param pageNum
     * @param pageSize
     */
    public static void startPage(int pageNum, int pageSize) {
        localPage.set(new Page(pageNum, pageSize));
    }

    /**
     * 如果开启了分页功能，则关闭分页
     */
    public static void endIfPaging() {
        if (localPage.get() != null) {
            localPage.remove();
        }
    }

    /**
     * 结束分页并返回结果，该方法必须被调用，否则localPage会一直保存下去，直到下一次startPage
     *
     * @return
     */
    public static Page endPage() {
        Page page = localPage.get();
        localPage.remove();
        return page;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (localPage.get() == null) {
            return invocation.proceed();
        } else {
            MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
            //分页信息
            Page page = localPage.get();
            MetaObject msObject = SystemMetaObject.forObject(ms);
            List<SqlNode> contents = (List<SqlNode>) msObject.getValue("sqlSource.rootSqlNode.contents");
            //求count - 重写sql
            contents.add(0, new TextSqlNode("select count(0) from ("));
            contents.add(new TextSqlNode(")"));
            Class<?> resultType = (Class<?>) msObject.getValue("resultMaps[0].type");
            List<ResultMapping> resultMappings = (List<ResultMapping>) msObject.getValue("resultMaps[0].resultMappings");
            msObject.setValue("resultMaps[0].type", int.class);
            msObject.setValue("resultMaps[0].resultMappings", EMPTY_RESULTMAPPING);
            Object result = null;
            try {
                //查询总数
                result = invocation.proceed();
                int totalCount = Integer.parseInt(((List) result).get(0).toString());
                page.setTotal(totalCount);
                int totalPage = totalCount / page.getPageSize() + ((totalCount % page.getPageSize() == 0) ? 0 : 1);
                page.setPages(totalPage);
            } finally {
                //清理count sql
                contents.remove(0);
                contents.remove(contents.size() - 1);
                //恢复类型
                msObject.setValue("resultMaps[0].type", resultType);
                msObject.setValue("resultMaps[0].resultMappings", resultMappings);
            }
            //分页sql
            contents.add(0, new TextSqlNode("select * from ( select temp.*, rownum row_id from ( "));
            StringBuilder pageSql = new StringBuilder(200);
            pageSql.append(" ) temp where rownum <= ").append(page.getEndRow());
            pageSql.append(") where row_id > ").append(page.getStartRow());
            contents.add(new TextSqlNode(pageSql.toString()));
            try {
                //执行分页查询
                result = invocation.proceed();
            } finally {
                //清理分页sql
                contents.remove(0);
                contents.remove(contents.size() - 1);
            }
            //得到处理结果
            page.setResult((List) result);
            //返回结果
            return result;
        }
    }

    /**
     * 只拦截这两种类型的
     * <br>StatementHandler
     * <br>ResultSetHandler
     *
     * @param target
     * @return
     */
    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        } else {
            return target;
        }
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
