package com.czetsuyatech.nerv.examples.event.order;

import java.time.Instant;
import java.util.UUID;

public record OrderResponse(UUID id, String customerId, String status, Instant createdAt, String eventId) {
}
