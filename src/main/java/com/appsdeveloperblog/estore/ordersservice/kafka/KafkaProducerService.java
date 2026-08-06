package com.appsdeveloperblog.estore.ordersservice.kafka;

import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import com.trutran.estore.core.kafka.ProductReservePubSub;


@Service
public class KafkaProducerService {
    private final KafkaTemplate<String, ProductReservePubSub> _kafkaTemplate;
    public KafkaProducerService(KafkaTemplate<String, ProductReservePubSub> kafkaTemplate){
        this._kafkaTemplate = kafkaTemplate;
    }
    public CompletableFuture<SendResult<String, ProductReservePubSub>> sendMessage(String topic, ProductReservePubSub message){
        System.err.println("Sent message:" + message + "to topic:" + topic);
        return _kafkaTemplate.send(topic, message);
        
    }
}