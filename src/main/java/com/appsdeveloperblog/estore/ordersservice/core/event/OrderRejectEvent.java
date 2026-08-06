package com.appsdeveloperblog.estore.ordersservice.core.event;

import com.trutran.estore.core.models.OrderStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderRejectEvent {
    private String orderId;
    private String reason;
    private OrderStatus orderStatus = OrderStatus.REJECTED;
}
