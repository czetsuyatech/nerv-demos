package com.czetsuyatech.nerv.example.exception.retry;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class PaymentRetryState {

    private final AtomicInteger timeoutAttempts = new AtomicInteger();

    public int nextTimeoutAttempt() {
        return timeoutAttempts.incrementAndGet();
    }

    public int timeoutAttempts() {
        return timeoutAttempts.get();
    }

    public void reset() {
        timeoutAttempts.set(0);
    }
}
