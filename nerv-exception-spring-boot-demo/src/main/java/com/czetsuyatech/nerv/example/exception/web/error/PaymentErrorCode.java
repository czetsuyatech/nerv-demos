package com.czetsuyatech.nerv.example.exception.web.error;

import com.czetsuyatech.nerv.exception.api.NervErrorCode;
import org.springframework.http.HttpStatus;

public enum PaymentErrorCode implements NervErrorCode {

  PAYMENT_NOT_FOUND(
      "PAYMENT_NOT_FOUND",
      "Payment was not found",
      HttpStatus.NOT_FOUND.value(),
      false,
      "BUSINESS"
  ),

  PAYMENT_TIMEOUT(
      "PAYMENT_TIMEOUT",
      "Payment provider timed out",
      HttpStatus.GATEWAY_TIMEOUT.value(),
      true,
      "INTEGRATION"
  );

  private final String code;
  private final String message;
  private final int status;
  private final boolean retryable;
  private final String category;

  PaymentErrorCode(
      String code,
      String message,
      int status,
      boolean retryable,
      String category
  ) {
    this.code = code;
    this.message = message;
    this.status = status;
    this.retryable = retryable;
    this.category = category;
  }

  @Override
  public String code() {
    return code;
  }

  @Override
  public String message() {
    return message;
  }

  @Override
  public int status() {
    return status;
  }

  @Override
  public boolean retryable() {
    return retryable;
  }

  @Override
  public String category() {
    return category;
  }
}
