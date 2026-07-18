package com.czetsuyatech.nerv.example.exception.feign.provider;

import com.czetsuyatech.nerv.example.exception.error.PaymentErrorCode;
import com.czetsuyatech.nerv.example.exception.retry.PaymentRetryState;
import com.czetsuyatech.nerv.exception.core.NervException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PaymentRetryProviderController {

  private final PaymentRetryState retryState;

  @GetMapping("/provider/retry/payments/timeout")
  public String timeoutThenSuccess() {
    int attempt = retryState.nextTimeoutAttempt();

    log.debug("Provider retry attempt: {}", attempt);

    if (attempt < 3) {
      throw NervException.of(PaymentErrorCode.PAYMENT_TIMEOUT);
    }

    return "Payment provider succeeded on attempt " + attempt;
  }

  @GetMapping("/provider/retry/reset")
  public String reset() {
    retryState.reset();
    return "Retry state reset";
  }

  @GetMapping("/provider/retry/attempts")
  public int attempts() {
    return retryState.timeoutAttempts();
  }
}
