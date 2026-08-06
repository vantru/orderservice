package com.appsdeveloperblog.estore.ordersservice.data;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, String> {
    OrderEntity findByOrderId(String orderId);
} 
