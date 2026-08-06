package com.appsdeveloperblog.estore.ordersservice.command;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

import com.appsdeveloperblog.estore.ordersservice.core.event.OrderApprovedEvent;
import com.appsdeveloperblog.estore.ordersservice.core.event.OrderRejectEvent;
import com.trutran.estore.core.events.OrderCreatedEvent;
import com.trutran.estore.core.models.OrderStatus;

import lombok.NoArgsConstructor;

@Aggregate
@NoArgsConstructor
public class OrderAggregate {
  @AggregateIdentifier
  public String orderId;
  private String userId;
  private String productId;
  private int quantity;
  private String addressId;
  private OrderStatus orderStatus;

  @CommandHandler
  public OrderAggregate(CreateOrderCommand createOrderCommand){// validate bussiness logic and then applyclyc
    
    if(createOrderCommand.getQuantity() <= 0){
      throw new IllegalArgumentException("Quantity can not be less 0");
    }

    OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent();

    BeanUtils.copyProperties(createOrderCommand, orderCreatedEvent);

    AggregateLifecycle.apply(orderCreatedEvent);
  }

  @EventSourcingHandler
  public void on(OrderCreatedEvent order){
    this.orderId = order.getOrderId();
    this.userId = order.getUserId();
    this.productId = order.getProductId();
    this.quantity = order.getQuantity();
    this.addressId = order.getAddressId();
    this.orderStatus = order.getOrderStatus();
  }

  @CommandHandler
  public void handler(ApproveOrderCommand command){
    OrderApprovedEvent orderApprovedEvent = new OrderApprovedEvent(command.getOrderId());
    AggregateLifecycle.apply(orderApprovedEvent);
  }

  @EventSourcingHandler
  protected void on(OrderApprovedEvent event){
    this.orderStatus = event.getOrderStatus();
  }

  @CommandHandler
  public void handler(RejectOrderCommand command){
    OrderRejectEvent orderRejectEvent = OrderRejectEvent.builder()
    .orderId(command.getOrderId())
    .reason(command.getReason()).build();

    AggregateLifecycle.apply(orderRejectEvent);
  }

  @EventSourcingHandler
  public void on(OrderRejectEvent event){
    this.orderStatus = event.getOrderStatus();
  }
}
