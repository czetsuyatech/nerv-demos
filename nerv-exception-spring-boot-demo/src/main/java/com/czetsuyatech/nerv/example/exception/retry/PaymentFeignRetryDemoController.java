package com.czetsuyatech.nerv.example.exception.retry;

import com.czetsuyatech.nerv.example.exception.feign.PaymentProviderClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PaymentFeignRetryDemoController {

    private final PaymentProviderClient paymentProviderClient;

    @GetMapping("/feign/retry/reset")
    public String reset() {
        return paymentProviderClient.resetRetry();
    }

    @GetMapping("/feign/retry/timeout")
    public String retryTimeout() {
        return paymentProviderClient.retryTimeout();
    }

    @GetMapping("/feign/retry/attempts")
    public int attempts() {
        return paymentProviderClient.retryAttempts();
    }
}
