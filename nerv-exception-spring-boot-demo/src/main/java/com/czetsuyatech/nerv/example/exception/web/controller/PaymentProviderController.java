package com.czetsuyatech.nerv.example.exception.web.controller;

import com.czetsuyatech.nerv.example.exception.error.PaymentErrorCode;
import com.czetsuyatech.nerv.exception.core.NervException;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentProviderController {

  @GetMapping("/provider/payments/not-found")
  public String notFound() {
    throw new NervException(PaymentErrorCode.PAYMENT_NOT_FOUND);
  }

  @GetMapping("/provider/payments/timeout")
  public String timeout() {
    throw new NervException(PaymentErrorCode.PAYMENT_TIMEOUT);
  }

  @GetMapping("/provider/trace")
  public Map<String, String> trace(
      @RequestHeader(value = "nerv-trace-id", required = false) String traceId,
      @RequestHeader(value = "nerv-span-id", required = false) String spanId
  ) {
    return Map.of(
        "traceId", traceId,
        "spanId", spanId
    );
  }
}
