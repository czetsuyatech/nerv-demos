package com.czetsuyatech.nerv.example.exception.web.controller;

import com.czetsuyatech.nerv.example.exception.error.PaymentErrorCode;
import com.czetsuyatech.nerv.exception.core.NervException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class PaymentController {

  @GetMapping("/payments/{id}")
  public String findPayment(@PathVariable String id) {
    if ("404".equals(id)) {
      throw new NervException(PaymentErrorCode.PAYMENT_NOT_FOUND);
    }

    if ("timeout".equals(id)) {
      throw new NervException(PaymentErrorCode.PAYMENT_TIMEOUT);
    }

    if ("error".equals(id)) {
      throw new RuntimeException("Unexpected failure");
    }

    return "Payment found: " + id;
  }
}
