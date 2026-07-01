package com.czetsuyatech.nerv.example.exception.retry;

import com.czetsuyatech.nerv.example.exception.error.PaymentErrorCode;
import com.czetsuyatech.nerv.exception.core.NervException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RetryableDemoController {

    @GetMapping("/retryable/business")
    public String businessError() {
        throw new NervException(PaymentErrorCode.PAYMENT_NOT_FOUND);
    }

    @GetMapping("/retryable/integration")
    public String integrationError() {
        throw new NervException(PaymentErrorCode.PAYMENT_TIMEOUT);
    }
}
