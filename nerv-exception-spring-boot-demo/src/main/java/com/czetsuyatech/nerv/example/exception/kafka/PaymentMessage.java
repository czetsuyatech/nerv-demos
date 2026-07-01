package com.czetsuyatech.nerv.example.exception.kafka;

public record PaymentMessage(
    String paymentId,
    String status
) {

}
