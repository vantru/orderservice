package com.appsdeveloperblog.estore.ordersservice.query;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import com.appsdeveloperblog.estore.ordersservice.core.event.OrderRejectEvent;
import com.appsdeveloperblog.estore.ordersservice.data.OrderEntity;
import com.appsdeveloperblog.estore.ordersservice.data.OrderRepository;

@Component
public class OrderRejectedEventHandler {
    private final OrderRepository orderRepository;

    public OrderRejectedEventHandler( OrderRepository orderRepository){
        this.orderRepository = orderRepository;
    }
    @EventHandler
    public void handler(OrderRejectEvent event){
        OrderEntity orderEntity = orderRepository.findByOrderId(event.getOrderId());
        if(orderEntity != null){
            orderEntity.setOrderStatus(event.getOrderStatus());
            orderRepository.save(orderEntity);
        }
    }
}
