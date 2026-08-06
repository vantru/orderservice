package com.appsdeveloperblog.estore.ordersservice.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("customCheck")
public class CustomReadinessCheck implements HealthIndicator {
    @Override
    public Health health() {
        // Return Health.up() or Health.down()
        return Health.up().build();
    }
}
