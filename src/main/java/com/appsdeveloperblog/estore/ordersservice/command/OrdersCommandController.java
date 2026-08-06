package com.appsdeveloperblog.estore.ordersservice.command;

import java.util.UUID;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.appsdeveloperblog.estore.ordersservice.command.rest.CreateOrderRest;
import com.trutran.estore.core.models.OrderStatus;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping("/orders")
public class OrdersCommandController {

    private final CommandGateway commandGateway;

    public OrdersCommandController(CommandGateway commandGateway){
        this.commandGateway = commandGateway;
    }
    
    @PostMapping("")
    public String createOrder(@Valid @RequestBody CreateOrderRest createOrderRest){
        String rs = "";
        CreateOrderCommand ordercommand = CreateOrderCommand.builder()
        .orderId(UUID.randomUUID().toString())
        .userId("27b95829-4f3f-4ddf-8983-151ba010e35b")
        .productId(createOrderRest.getProductId())
        .quantity(createOrderRest.getQuantity())
        .addressId(createOrderRest.getAddressId())
        .orderStatus(OrderStatus.CREATED)
        .build();
        try{
           rs = commandGateway.sendAndWait(ordercommand);
        }
        catch(Exception ex){
            rs = ex.getLocalizedMessage();
        }
        return rs;
    }
}
