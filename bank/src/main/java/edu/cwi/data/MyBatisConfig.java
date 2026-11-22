package edu.cwi.data;

import java.util.function.Function;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public final class MyBatisConfig implements AutoCloseable {
    private final DataSource dataSource;
    private final SqlSessionFactory factory;

    // DataSource injection lets tests use an isolated database.
    public MyBatisConfig(DataSource dataSource) {
        this.dataSource = dataSource;
        Environment environment = new Environment(
                "bank", new JdbcTransactionFactory(), dataSource);
        Configuration config = new Configuration(environment);
        config.setMapUnderscoreToCamelCase(true);
        config.addMapper(UserMapper.class);
        config.addMapper(AccountMapper.class);
        config.addMapper(TransactionMapper.class);
        factory = new SqlSessionFactoryBuilder().build(config);
    }

    public static MyBatisConfig fromEnvironment() {
        HikariConfig pool = new HikariConfig();
        pool.setJdbcUrl(System.getenv().getOrDefault(
                "DB_URL", "jdbc:mysql://localhost:3306/online_bank"));
        pool.setUsername(required("DB_USER"));
        pool.setPassword(required("DB_PASSWORD"));
        pool.setDriverClassName("com.mysql.cj.jdbc.Driver");
        pool.setMaximumPoolSize(5);
        pool.setMinimumIdle(1);
        pool.setConnectionTimeout(10_000);
        pool.setPoolName("OnlineBankPool");
        return new MyBatisConfig(new HikariDataSource(pool));
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Set the " + name + " environment variable before running.");
        }
        return value;
    }

    public SqlSessionFactory getSqlSessionFactory() {
        return factory;
    }

    public SqlSession openSession() {
        return factory.openSession(false);
    }

    // Every operation in work shares one connection and one transaction.
    public <T> T write(Function<SqlSession, T> work) {
        try (SqlSession session = openSession()) {
            try {
                T result = work.apply(session);
                session.commit();
                return result;
            } catch (RuntimeException e) {
                session.rollback();
                throw e;
            }
        }
    }

    @Override
    public void close() {
        if (dataSource instanceof HikariDataSource pool) {
            pool.close();
        }
    }
}
