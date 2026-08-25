package com.czetsuyatech.nerv.examples.event.order;

import java.time.Instant;
import java.util.UUID;

public record OrderCreated(UUID orderId, String customerId, Instant createdAt) {
}
