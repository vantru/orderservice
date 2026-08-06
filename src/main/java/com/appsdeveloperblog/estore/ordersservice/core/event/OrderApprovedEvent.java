package com.appsdeveloperblog.estore.ordersservice.core.event;

import com.trutran.estore.core.models.OrderStatus;

import lombok.Value;

@Value
public class OrderApprovedEvent {
    private final String orderId;
    private final OrderStatus orderStatus = OrderStatus.APPROVED;
}
