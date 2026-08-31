package com.czetsuyatech.nerv.examples.event.interceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.czetsuyatech.nerv.event.model.EventId;
import com.czetsuyatech.nerv.event.model.EventMessage;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

class EventContextInterceptorTest {

  @Test
  void scopesEventContextToTheHandlerExecution() {
    EventContextInterceptor interceptor = new EventContextInterceptor();
    EventMessage<String> event = new EventMessage<>(
        new EventId("event-1"),
        "order.created",
        Instant.parse("2026-08-31T00:00:00Z"),
        "orders",
        "correlation-1",
        "payload"
    );

    MDC.put("nerv.event.id", "parent-event");
    MDC.put("nerv.event.type", "parent.type");
    MDC.put("nerv.event.correlation_id", "parent-correlation");
    try {
      interceptor.intercept(event, () -> {
        assertThat(MDC.get("nerv.event.id")).isEqualTo("event-1");
        assertThat(MDC.get("nerv.event.type")).isEqualTo("order.created");
        assertThat(MDC.get("nerv.event.correlation_id")).isEqualTo("correlation-1");
      });

      assertThat(MDC.get("nerv.event.id")).isEqualTo("parent-event");
      assertThat(MDC.get("nerv.event.type")).isEqualTo("parent.type");
      assertThat(MDC.get("nerv.event.correlation_id")).isEqualTo("parent-correlation");
    } finally {
      MDC.clear();
    }
  }

  @Test
  void restoresThePreviousContextWhenTheHandlerFails() {
    EventContextInterceptor interceptor = new EventContextInterceptor();
    MDC.put("nerv.event.id", "parent-event");
    try {
      assertThatThrownBy(() -> interceptor.intercept(
          event(),
          () -> {
            throw new IllegalStateException("handler failure");
          }
      )).isInstanceOf(IllegalStateException.class)
          .hasMessage("handler failure");

      assertThat(MDC.get("nerv.event.id")).isEqualTo("parent-event");
    } finally {
      MDC.clear();
    }
  }

  @Test
  void nestedInterceptorsRestoreTheOuterContext() {
    EventContextInterceptor interceptor = new EventContextInterceptor();
    EventMessage<String> outerEvent = event();
    EventMessage<String> innerEvent = new EventMessage<>(
        new EventId("event-2"),
        "payment.requested",
        Instant.parse("2026-08-31T00:00:01Z"),
        "payments",
        "correlation-2",
        "payload"
    );
    MDC.put("nerv.event.id", "parent-event");
    try {
      interceptor.intercept(outerEvent, () -> {
        assertThat(MDC.get("nerv.event.id")).isEqualTo("event-1");
        interceptor.intercept(innerEvent, () -> assertThat(MDC.get("nerv.event.id")).isEqualTo("event-2"));
        assertThat(MDC.get("nerv.event.id")).isEqualTo("event-1");
      });

      assertThat(MDC.get("nerv.event.id")).isEqualTo("parent-event");
    } finally {
      MDC.clear();
    }
  }

  private static EventMessage<String> event() {
    return new EventMessage<>(
        new EventId("event-1"),
        "order.created",
        Instant.parse("2026-08-31T00:00:00Z"),
        "orders",
        "correlation-1",
        "payload"
    );
  }
}
