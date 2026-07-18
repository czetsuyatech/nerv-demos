package com.czetsuyatech.nerv.example.exception.feign.provider;

import com.czetsuyatech.nerv.example.exception.error.PaymentErrorCode;
import com.czetsuyatech.nerv.exception.core.NervException;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class PaymentProviderController {

  private final Tracer tracer;

  @GetMapping("/provider/payments/not-found")
  public String notFound() {
    throw NervException.of(PaymentErrorCode.PAYMENT_NOT_FOUND);
  }

  @GetMapping("/provider/payments/timeout")
  public String timeout() {
    throw  NervException.of(PaymentErrorCode.PAYMENT_TIMEOUT);
  }
}
