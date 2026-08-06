package com.appsdeveloperblog.estore.ordersservice.data;

import com.trutran.estore.core.models.OrderStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "orders")
public class OrderEntity {
    @Id
    @Column(unique = true)
    private  String orderId;
    private  String userId;
    private  String productId;
    private  int quantity;
    private  String addressId;
    @Enumerated(EnumType.STRING)
    private  OrderStatus orderStatus;
}
