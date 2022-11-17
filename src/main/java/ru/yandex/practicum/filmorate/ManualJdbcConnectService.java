package ru.yandex.practicum.filmorate;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

@Component
public class ManualJdbcConnectService {
    public static final String JDBC_URL = "jdbc:postgresql://localhost:5432/cats";
    public static final String JDBC_LOGIN = "admin";
    public static final String JDBC_PASSWORD = "admin";
    public static final String JDBC_DRIVER = "org.postgresql.Driver";

    public JdbcTemplate getTemplate() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(JDBC_URL);
        dataSource.setUsername(JDBC_LOGIN);
        dataSource.setPassword(JDBC_PASSWORD);
        dataSource.setDriverClassName(JDBC_DRIVER);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate;
    }
}