package com.appsdeveloperblog.estore.ordersservice.config;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DataSourceProperties.class)
public class DataSourceConfig {

    @Bean
    public DataSource dataSource(DataSourceProperties props){
        return DataSourceBuilder.create()
        .url(props.url()).username(props.username()).password(props.password())
        .driverClassName("org.postgresql.Driver")
        .build();
    }
}
