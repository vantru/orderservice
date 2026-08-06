package com.appsdeveloperblog.estore.ordersservice.query;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;


import com.appsdeveloperblog.estore.ordersservice.data.OrderEntity;
import com.appsdeveloperblog.estore.ordersservice.data.OrderRepository;
import com.appsdeveloperblog.estore.ordersservice.data.OutBoxEventRepository;
import com.appsdeveloperblog.estore.ordersservice.data.OutboxEventEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trutran.estore.core.events.OrderCreatedEvent;

import jakarta.transaction.Transactional;

@Component
public class OrderCreatedEventHandler {
    
    private final OrderRepository orderRepository;
    private final OutBoxEventRepository outBoxEventRepository;
    private final ObjectMapper objectMapper;
    public OrderCreatedEventHandler(OrderRepository orderRepository, OutBoxEventRepository outBoxEventRepository,ObjectMapper objectMapper){
        this.orderRepository = orderRepository;
        this.outBoxEventRepository = outBoxEventRepository;
        this.objectMapper = objectMapper;
    }

    @EventHandler
    @Transactional
    public void on(OrderCreatedEvent orderCreatedEvent) throws JsonProcessingException{
        OrderEntity orderEntity = new OrderEntity();
        BeanUtils.copyProperties(orderCreatedEvent, orderEntity);

        this.orderRepository.save(orderEntity);

        OutboxEventEntity outboxEventEntity = new OutboxEventEntity();
        outboxEventEntity.setAggregateType("Order");
        outboxEventEntity.setEventType("OrderCreated");
        outboxEventEntity.setAggregateId(orderCreatedEvent.getOrderId());
        outboxEventEntity.setPayload(objectMapper.writeValueAsString(orderCreatedEvent));
        outBoxEventRepository.save(outboxEventEntity);

    }
}
