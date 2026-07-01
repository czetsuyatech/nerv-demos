package com.czetsuyatech.nerv.example.exception.web.controller;

import com.czetsuyatech.nerv.example.exception.feign.PaymentProviderClient;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PaymentFeignDemoController {

  private final PaymentProviderClient paymentProviderClient;

  @GetMapping("/feign/payments/not-found")
  public String notFound() {
    return paymentProviderClient.notFound();
  }

  @GetMapping("/feign/payments/timeout")
  public String timeout() {
    return paymentProviderClient.timeout();
  }

  @GetMapping("/feign/trace")
  public Map<String, String> trace() {
    return paymentProviderClient.trace();
  }
}
