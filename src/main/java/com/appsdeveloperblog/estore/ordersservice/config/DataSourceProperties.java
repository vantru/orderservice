package com.appsdeveloperblog.estore.ordersservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.datasource")
public record DataSourceProperties(String url, String username, String password) {
    
}
