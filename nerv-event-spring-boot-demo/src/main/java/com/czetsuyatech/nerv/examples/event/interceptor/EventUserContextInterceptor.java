package com.czetsuyatech.nerv.examples.event.interceptor;

import com.czetsuyatech.nerv.event.consumer.EventHandlerChain;
import com.czetsuyatech.nerv.event.consumer.EventHandlerInterceptor;
import com.czetsuyatech.nerv.event.model.EventMessage;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Demonstrates initialization and restoration of a ThreadLocal execution context around a handler.
 */
@Component
@Order(-100)
public class EventUserContextInterceptor implements EventHandlerInterceptor {

  @Override
  public void intercept(
      EventMessage<?> event,
      EventHandlerChain chain
  ) {
    EventUserContext previous = EventUserContextHolder.get();
    try {
      EventUserContextHolder.set(new EventUserContext(
          event.source(),
          event.correlationId()
      ));
      chain.proceed();
    } finally {
      if (previous == null) {
        EventUserContextHolder.clear();
      } else {
        EventUserContextHolder.set(previous);
      }
    }
  }
}
