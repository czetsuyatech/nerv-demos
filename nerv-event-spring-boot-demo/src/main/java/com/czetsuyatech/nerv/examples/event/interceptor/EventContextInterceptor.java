package com.czetsuyatech.nerv.examples.event.interceptor;

import com.czetsuyatech.nerv.event.consumer.EventHandlerChain;
import com.czetsuyatech.nerv.event.consumer.EventHandlerInterceptor;
import com.czetsuyatech.nerv.event.model.EventMessage;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Demonstrates broker-neutral event context setup and cleanup around every resolved event handler.
 */
@Component
@Order(0)
public class EventContextInterceptor implements EventHandlerInterceptor {

  @Override
  public void intercept(
      EventMessage<?> event,
      EventHandlerChain chain
  ) {
    Map<String, String> previousContext = MDC.getCopyOfContextMap();
    try {
      populate(event);
      chain.proceed();
    } finally {
      restore(previousContext);
    }
  }

  private static void populate(EventMessage<?> event) {
    MDC.put(
        "nerv.event.id",
        event.id().value()
    );
    MDC.put(
        "nerv.event.type",
        event.type()
    );
    if (event.correlationId() == null) {
      MDC.remove("nerv.event.correlation_id");
    } else {
      MDC.put(
          "nerv.event.correlation_id",
          event.correlationId()
      );
    }
  }

  private static void restore(Map<String, String> context) {
    MDC.clear();
    if (context != null) {
      MDC.setContextMap(context);
    }
  }
}
