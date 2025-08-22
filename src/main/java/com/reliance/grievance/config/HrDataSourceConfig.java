package com.reliance.grievance.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class HrDataSourceConfig {

    @Bean(name = "hrDataSource")
    @ConfigurationProperties(prefix = "hr.datasource")
    public DataSource hrDataSource() {
        return DataSourceBuilder.create()
                .type(com.zaxxer.hikari.HikariDataSource.class)
                .build();
    }

    @Bean(name = "hrJdbc")
    public JdbcTemplate hrJdbc(@Qualifier("hrDataSource") DataSource ds) {
        // safety net: ensure catalog is EmployeeMaster even if properties are off
        if (ds instanceof com.zaxxer.hikari.HikariDataSource hikari) {
            hikari.setCatalog("EmployeeMaster");
        }
        return new JdbcTemplate(ds);
    }
}

