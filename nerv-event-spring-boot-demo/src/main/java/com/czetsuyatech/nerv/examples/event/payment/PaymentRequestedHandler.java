package com.czetsuyatech.nerv.examples.event.payment;

import com.czetsuyatech.nerv.event.consumer.EventHandler;
import com.czetsuyatech.nerv.event.exception.EventRetryableException;
import com.czetsuyatech.nerv.event.model.EventMessage;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Deliberately deterministic inbox outcomes: RETRY-ONCE fails once with the public retryable
 * exception, while FAIL-PERMANENT becomes FAILED until the documented recovery property is set.
 */
@Component
public class PaymentRequestedHandler implements EventHandler<PaymentRequested> {

  private final Set<String> retryAttempts = ConcurrentHashMap.newKeySet();
  private final PaymentFailureSwitch failureSwitch;

  public PaymentRequestedHandler(PaymentFailureSwitch failureSwitch) {
    this.failureSwitch = failureSwitch;
  }

  @Override
  public String eventType() {
    return "payment.requested";
  }

  @Override
  public Class<PaymentRequested> payloadType() {
    return PaymentRequested.class;
  }

  @Override
  public void handle(EventMessage<PaymentRequested> event) {
    String customerId = event.payload().customerId();
    if ("RETRY-ONCE".equals(customerId) && retryAttempts.add(event.id().value())) {
      throw new EventRetryableException("Demo payment processor is temporarily unavailable");
    }
    if ("FAIL-PERMANENT".equals(customerId) && !failureSwitch.permanentFailureRecoveryAllowed()) {
      throw new IllegalStateException("Demo payment validation failed permanently");
    }
  }
}
