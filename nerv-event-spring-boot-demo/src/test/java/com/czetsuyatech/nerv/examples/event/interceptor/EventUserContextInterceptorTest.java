package com.czetsuyatech.nerv.examples.event.interceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.czetsuyatech.nerv.event.model.EventId;
import com.czetsuyatech.nerv.event.model.EventMessage;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class EventUserContextInterceptorTest {

  private final EventUserContextInterceptor interceptor = new EventUserContextInterceptor();

  @AfterEach
  void clearContext() {
    EventUserContextHolder.clear();
  }

  @Test
  void initializesUserContextForTheHandlerAndRestoresThePreviousValue() {
    EventUserContext previous = new EventUserContext(
        "parent",
        "parent-correlation"
    );
    EventUserContextHolder.set(previous);

    interceptor.intercept(event(), () -> assertThat(EventUserContextHolder.get()).isEqualTo(
        new EventUserContext(
            "orders",
            "correlation-1"
        )
    ));

    assertThat(EventUserContextHolder.get()).isSameAs(previous);
  }

  @Test
  void restoresThePreviousUserContextWhenTheHandlerFails() {
    EventUserContext previous = new EventUserContext(
        "parent",
        "parent-correlation"
    );
    EventUserContextHolder.set(previous);

    assertThatThrownBy(() -> interceptor.intercept(
        event(),
        () -> {
          throw new IllegalStateException("handler failure");
        }
    )).isInstanceOf(IllegalStateException.class)
        .hasMessage("handler failure");

    assertThat(EventUserContextHolder.get()).isSameAs(previous);
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
