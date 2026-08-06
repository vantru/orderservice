package com.appsdeveloperblog.estore.ordersservice.query;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.appsdeveloperblog.estore.ordersservice.core.event.OrderApprovedEvent;
import com.appsdeveloperblog.estore.ordersservice.data.OrderEntity;
import com.appsdeveloperblog.estore.ordersservice.data.OrderRepository;

import lombok.Value;

@Component
public class OrderApprovedEventHandler {

    private final OrderRepository orderRepository;

    public OrderApprovedEventHandler(OrderRepository repository)
    {
        this.orderRepository = repository;
    }

    public void on(OrderApprovedEvent event){
        Optional< OrderEntity> orderEntityOp = orderRepository.findById(event.getOrderId());
        if(!orderEntityOp.isPresent()){
             // ("can not found orders by id: " + event.getOrderId());
             return;
        }
        OrderEntity orderEntity = orderEntityOp.get();
        orderEntity.setOrderStatus(event.getOrderStatus());
        orderRepository.save(orderEntity);
    }
}
