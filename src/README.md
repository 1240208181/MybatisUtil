#Mybatis获取转换后的sql语句

需要添加如下依赖：

注意要添加自己的sql数据库链接
```xml
    <dependencies>
    <dependency>
        <groupId>commons-beanutils</groupId>
        <artifactId>commons-beanutils</artifactId>
        <version>1.9.2</version>
    </dependency>
    <dependency>
        <groupId>org.mybatis</groupId>
        <artifactId>mybatis</artifactId>
        <version>3.2.6</version>
        <scope>compile</scope>
        <optional>true</optional>
    </dependency>
    <!-- Mybatis Generator -->
    <dependency>
        <groupId>org.mybatis.generator</groupId>
        <artifactId>mybatis-generator-core</artifactId>
        <version>1.3.2</version>
        <scope>compile</scope>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.31</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <version>4.11</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>log4j</groupId>
        <artifactId>log4j</artifactId>
        <version>1.2.17</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

用法：
    获取connection,sql语句,对应参数map,使用MybatisUtil.parseMyBatisStatement(SqlSessionFactory sqlSessionFactory, String sql, Object parameter)
    即可获取转换后的sql语句