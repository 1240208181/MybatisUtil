import org.apache.commons.collections.CollectionUtils;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;

/**
 * @author zhusiyuan
 * @date 2023/8/19
 * @apiNote
 */
public class MybatisUtil {
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final TransactionFactory TRANSACTION_FACTORY = new JdbcTransactionFactory();
    private static SqlSessionFactory sqlSessionFactory =null;
    public static SqlSessionFactory getFactory(Connection connection) throws SQLException {
        // 如果要连接多个不同数据库
        // 也可以自己建一个对象存储url,driverName,clientInfo 重写hashCode和equals，放到map中使用单例模式
        DatabaseMetaData metaData = connection.getMetaData();
        String url = metaData.getURL();
        String driverName = metaData.getDriverName();
        Properties clientInfo = connection.getClientInfo();
        // 连接数据源
        PooledDataSource pooledDataSource = new PooledDataSource(driverName, url, clientInfo);
        Environment environment = new Environment("id", TRANSACTION_FACTORY, pooledDataSource);
        Configuration configuration =new Configuration(environment);
        // 构建SqlSessionFactory

        return new SqlSessionFactoryBuilder().build(configuration);
    }

    public static String parseMyBatisStatement(Connection connection, String sql, Object parameter) throws SQLException {
        // 单例模式,可以自己看情况加入双检索单例模式
        if(sqlSessionFactory == null){
            sqlSessionFactory = getFactory(connection);
        }
        Configuration configuration = sqlSessionFactory.getConfiguration();
        // 创建 LanguageDriver
        XMLLanguageDriver languageDriver = new XMLLanguageDriver();

        // 创建 SqlSource
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, parameter.getClass());

        // 创建 MappedStatement 对象（ID 随意取，SqlCommandType 请根据实际情况设置）
        MappedStatement.Builder builder = new MappedStatement.Builder(
                configuration, "dummyId", sqlSource, SqlCommandType.SELECT
        );
        MappedStatement mappedStatement = builder.build();

        // 创建 BoundSql 对象
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        return showSql(configuration, boundSql);

    }

    /**
     * 如果参数是String，则添加单引号，
     * 如果是日期，则转换为时间格式器并加单引号； 对参数是null和不是null的情况作了处理
     *
     * @param obj 参数
     * @return String
     */
    private static String getParameterValue(Object obj) {
        String value;
        if (obj instanceof String) {
            value = "'" + obj + "'";
        } else if (obj instanceof Date) {
            value = "'" + formatter.format(obj) + "'";
        } else {
            if (obj != null) {
                value = obj.toString();
            } else {
                value = "";
            }
        }
        return value;
    }

    /**
     * 进行?的替换
     *
     * @param configuration 配置
     * @param boundSql      boundSql
     * @return String
     */
    public static String  showSql(Configuration configuration, BoundSql boundSql) {
        // 获取参数
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        // sql语句中多个空格都用一个空格代替
        String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
        if (!CollectionUtils.isEmpty(parameterMappings) && parameterObject != null) {
            // 获取类型处理器注册器，类型处理器的功能是进行java类型和数据库类型的转换
            // 如果根据parameterObject.getClass(）可以找到对应的类型，则替换
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(parameterObject)));
            } else {
                // MetaObject主要是封装了originalObject对象，提供了get和set的方法用于获取和设置originalObject的属性值,主要支持对JavaBean、Collection、Map三种类型对象的操作
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                for (ParameterMapping parameterMapping : parameterMappings) {
                    String propertyName = parameterMapping.getProperty();
                    if (metaObject.hasGetter(propertyName)) {
                        Object obj = metaObject.getValue(propertyName);
                        if (Objects.nonNull(obj)) {
                            sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(obj)));
                        } else {
                            sql = sql.replaceFirst("\\?", "null");
                        }
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        // 该分支是动态sql
                        Object obj = boundSql.getAdditionalParameter(propertyName);
                        if (Objects.nonNull(obj)) {
                            sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(obj)));
                        } else {
                            sql = sql.replaceFirst("\\?", "null");
                        }
                    }
                }
            }
        }
        return sql;
    }
}
