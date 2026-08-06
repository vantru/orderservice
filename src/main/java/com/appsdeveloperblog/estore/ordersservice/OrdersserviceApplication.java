package com.appsdeveloperblog.estore.ordersservice;

import java.time.ZoneId;
import java.util.TimeZone;

import org.axonframework.config.Configuration;
import org.axonframework.config.ConfigurationScopeAwareProvider;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.SimpleDeadlineManager;
import org.axonframework.spring.messaging.unitofwork.SpringTransactionManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.trutran.estore.core.config.XStreamConfig;

@SpringBootApplication
@Import(XStreamConfig.class)
public class OrdersserviceApplication {

	public static void main(String[] args) {
		System.out.println(System.getProperty("spring.datasource.url"));
		System.out.println("TimeZone = " + TimeZone.getDefault());
        System.out.println("ZoneId   = " + ZoneId.systemDefault());
		SpringApplication.run(OrdersserviceApplication.class, args);
	}

	@Bean
	public DeadlineManager deadlineManager(Configuration configuration, SpringTransactionManager transactionManager){
		return SimpleDeadlineManager.builder()
		.scopeAwareProvider(new ConfigurationScopeAwareProvider(configuration))
		.transactionManager(transactionManager)
		.build();
	}
}
