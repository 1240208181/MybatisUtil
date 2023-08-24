
import org.apache.commons.collections.CollectionUtils;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.junit.Test;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

public class MybatisUtilTest {

    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    @Test
    public void test() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection connection= DriverManager.getConnection("jdbc:mysql://localhost:3306/name?useSSL=false","root","123456");
            String sql= "<script>" +
                    "select * from bank " +
                    "   <where>" +
                    "       <if test=\" bank_name != '中国银行'\">" +
                    "           bank_name = #{bank_name}" +
                    "       </if>" +
                    "   </where>" +
                    "</script>";
            Map<String ,Object> map = new HashMap<>();
            map.put("id","");
            map.put("bank_name","中国农业银行");
            System.out.println(MybatisUtil.parseMyBatisStatement(connection,sql,map));

    }



}
