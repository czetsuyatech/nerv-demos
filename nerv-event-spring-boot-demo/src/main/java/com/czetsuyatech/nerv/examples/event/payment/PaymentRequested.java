package com.czetsuyatech.nerv.examples.event.payment;

import java.time.Instant;
import java.util.UUID;

public record PaymentRequested(UUID orderId, String customerId, Instant requestedAt) {
}
