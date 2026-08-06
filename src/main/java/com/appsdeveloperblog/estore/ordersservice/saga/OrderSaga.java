package com.appsdeveloperblog.estore.ordersservice.saga;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.appsdeveloperblog.estore.ordersservice.command.ApproveOrderCommand;
import com.appsdeveloperblog.estore.ordersservice.command.RejectOrderCommand;
import com.appsdeveloperblog.estore.ordersservice.core.event.OrderApprovedEvent;
import com.appsdeveloperblog.estore.ordersservice.core.event.OrderRejectEvent;
import com.appsdeveloperblog.estore.ordersservice.grpcclientservice.UserGrpcClientService;
import com.appsdeveloperblog.estore.ordersservice.kafka.KafkaProducerService;
import com.estore.user.grpc.UserResponse;
import com.trutran.estore.core.commands.CancelReservationProductCommand;
import com.trutran.estore.core.commands.ProcessPaymentCommand;
import com.trutran.estore.core.commands.ReserveProductCommand;
import com.trutran.estore.core.events.OrderCreatedEvent;
import com.trutran.estore.core.events.PaymentProcessedEvent;
import com.trutran.estore.core.events.ProductReservationCancelEvent;
import com.trutran.estore.core.events.ProductReserveEvent;
import com.trutran.estore.core.kafka.ProductReservePubSub;
import com.trutran.estore.core.models.PaymentDetails;
import com.trutran.estore.core.models.User;
import com.trutran.estore.core.query.FetchUserPaymentDetailsQuery;

@Saga
public class OrderSaga {

    @Autowired
    private transient CommandGateway commandGateway;

    // @Autowired
    // private transient KafkaProducerService kafkaProducerService;

    @Autowired 
    private transient UserGrpcClientService userGrpcClientService;

    @Autowired
    private transient DeadlineManager deadlineManager;

    private final String PAYMENT_PROCESS_TIMEOUT_DEADLINE="Payment-processing-deadline";

    private String scheduleId;

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(OrderSaga.class);

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handler(OrderCreatedEvent orderCreatedEvent){

        ReserveProductCommand reserveProductCommand = ReserveProductCommand.builder()
        .orderId(orderCreatedEvent.getOrderId())
        .productId(orderCreatedEvent.getProductId())
        .quantity(orderCreatedEvent.getQuantity())
        .userId(orderCreatedEvent.getUserId()).build();

        LOGGER.info("OrderCreatedEvent handler for orderId: " + orderCreatedEvent.getOrderId() + " and productId: " + orderCreatedEvent.getProductId());

         commandGateway.send(reserveProductCommand, new CommandCallback<ReserveProductCommand, Object>() {

            @Override
            public void onResult(CommandMessage<? extends ReserveProductCommand> commandMessage,
                    CommandResultMessage<?> commandResultMessage) {
                if(commandResultMessage.isExceptional()){
                    commandResultMessage.exceptionResult().printStackTrace();
                    //start compensating transaction
                }
                 else {
                    System.out.println("ReserveProductCommand handled");
                }
            }
            
        });

        //send to kafka
        // ProductReservePubSub productReservePubSub = ProductReservePubSub.builder()
        //  .orderId(orderCreatedEvent.getOrderId())
        // .productId(orderCreatedEvent.getProductId())
        // .quantity(orderCreatedEvent.getQuantity())
        // .userId(orderCreatedEvent.getUserId()).build();
        // this.kafkaProducerService.sendMessage("order-events", productReservePubSub)
        // .whenComplete((result, ex) -> {
        //     if(ex != null){
        //         LOGGER.error("Kafka send failed", ex);
        //         //compensating transaction
        //         //commandGateway.send(new RejectOrderCommand(...))
        //     }
        //     else{
        //         LOGGER.info("Kafka send success");
        //     }
        // });

       
     }

    @SagaEventHandler(associationProperty = "orderId")
    public void handler(ProductReserveEvent productReserveEvent){
        //Process user payment
        LOGGER.info("ProductReservedEvent is called for productId: " + productReserveEvent.getProductId() 
        + " and orderID: "+ productReserveEvent.getOrderId());
        UserResponse user = null;
        try{
            
            user = userGrpcClientService.getUser(productReserveEvent.getUserId());
            System.out.println(user.getFirstName());
            System.out.println(user.getPaymentDetails().getCardNumber());
        }
        catch(Exception ex){
            LOGGER.error(ex.getMessage());
            //start compensating transaction.
             cancelReservationProduct(productReserveEvent, ex.getMessage());
            return;
        }
        if(user == null){
            //start compensating transaction.
             cancelReservationProduct(productReserveEvent, "can not fetch user payment");
            return;
        }
      

         PaymentDetails paymentDetails = PaymentDetails.builder()
        .cardNumber(user.getPaymentDetails().getCardNumber())
        .cvv(user.getPaymentDetails().getCvv())
        .name(user.getPaymentDetails().getName())
        .validUntilMonth(user.getPaymentDetails().getValidUntilMonth())
        .validUntilYear(user.getPaymentDetails().getValidUntilYear()).build();

          LOGGER.info("Sucessfully fetched user payment details for user " + user.getFirstName());

        this.scheduleId = deadlineManager.schedule(Duration.of(10, ChronoUnit.SECONDS), PAYMENT_PROCESS_TIMEOUT_DEADLINE, productReserveEvent);

        //only use for testing.
       // if(true) return;

        ProcessPaymentCommand processPaymentCommand = ProcessPaymentCommand.builder()
        .orderId(productReserveEvent.getOrderId())
        .paymentDetails(paymentDetails)
        .paymentId(UUID.randomUUID().toString()).build();
        String result = null;
        try{
            result = commandGateway.sendAndWait(processPaymentCommand);
        }
        catch(Exception ex){
            LOGGER.error(ex.getMessage());
            //start compensating transaction
            cancelReservationProduct(productReserveEvent, ex.getMessage());
        }
        if(result == null){
            LOGGER.info("The processPaymentCommand resulted in null. Initiating a compens");
            //start compensation transaction
            cancelReservationProduct(productReserveEvent, "can not excuse payment");
        }
    }
    private void cancelReservationProduct(ProductReserveEvent productReserveEvent, String reason){

        cancelDeadline();

        CancelReservationProductCommand cancelReservationProductCommand = CancelReservationProductCommand.builder()
        .orderId(productReserveEvent.getOrderId())
        .reason(reason)
        .userId(productReserveEvent.getUserId())
        .productId(productReserveEvent.getProductId())
        .quantity(productReserveEvent.getQuantity()).build();

        commandGateway.send(cancelReservationProductCommand);
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handled(PaymentProcessedEvent event)
    {
        cancelDeadline();

        ApproveOrderCommand approveOrderCommand = new ApproveOrderCommand(event.getOrderId());
        commandGateway.send(approveOrderCommand);
        //send an ApproveOrderCommand.
    }

    private void cancelDeadline(){
        if(scheduleId != null){
            deadlineManager.cancelSchedule( PAYMENT_PROCESS_TIMEOUT_DEADLINE, scheduleId);
            scheduleId = null;
        }
        
    }


    @SagaEventHandler(associationProperty = "orderId")
    @EndSaga
    public void handled(OrderApprovedEvent event){
        LOGGER.info("Order os approved. order saga is complete for orderId "+ event.getOrderId());
        //SagaLifecycle.end();
    }

    
    @SagaEventHandler(associationProperty = "orderId")
    public void handled(ProductReservationCancelEvent event){
        RejectOrderCommand rejectOrderCommand = new RejectOrderCommand(event.getOrderId(), event.getReason());
        commandGateway.send(rejectOrderCommand);
    }

    @SagaEventHandler(associationProperty = "orderId")
    @EndSaga
    public void handled(OrderRejectEvent event){
        LOGGER.info("Successfully rejected order for orderId "+ event.getOrderId());
        //SagaLifecycle.end();
    }

    @DeadlineHandler(deadlineName = PAYMENT_PROCESS_TIMEOUT_DEADLINE)
    public void handledPaymentDeadline(ProductReserveEvent productReserveEvent){
        LOGGER.info("Payment processing deadline took place, sending a compensating command to cancel the ");
        cancelReservationProduct(productReserveEvent, "payment timeout");
    }
}
