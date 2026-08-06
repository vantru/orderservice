package com.appsdeveloperblog.estore.ordersservice.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApproveOrderCommand {
    @TargetAggregateIdentifier
    private final String orderId;
}
