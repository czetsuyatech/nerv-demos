package com.czetsuyatech.nerv.example.exception.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentKafkaProducer {

    private final KafkaTemplate<String, PaymentMessage> kafkaTemplate;

    public void publish(PaymentMessage message) {
        kafkaTemplate.send("payments", message.paymentId(), message);
    }
}
