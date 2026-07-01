package com.czetsuyatech.nerv.example.exception.feign;

import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
    name = "payment-provider",
    url = "http://localhost:${server.port}"
)
public interface PaymentProviderClient {

  @GetMapping("/provider/payments/timeout")
  String timeout();

  @GetMapping("/provider/payments/not-found")
  String notFound();

  @GetMapping("/provider/trace")
  Map<String, String> trace();

  @GetMapping("/provider/retry/payments/timeout")
  String retryTimeout();

  @GetMapping("/provider/retry/reset")
  String resetRetry();

  @GetMapping("/provider/retry/attempts")
  int retryAttempts();
}
