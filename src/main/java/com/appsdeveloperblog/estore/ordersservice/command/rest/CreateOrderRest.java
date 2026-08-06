package com.appsdeveloperblog.estore.ordersservice.command.rest;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderRest {
    
    @NotEmpty(message =  "ProductId can not be is empty")
    public  String productId;
    @Min(value = 1 ,message = "Quantity less or than 0")
    public  int quantity;
    @NotEmpty(message =  "AddressId can not be is empty")
    public  String addressId;
}
