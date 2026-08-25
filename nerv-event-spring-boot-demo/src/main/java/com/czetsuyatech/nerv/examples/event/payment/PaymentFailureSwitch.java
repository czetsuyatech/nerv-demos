package com.czetsuyatech.nerv.examples.event.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Application-owned demo toggle; the NERV operations endpoint still performs the actual retry. */
@Component
public class PaymentFailureSwitch {

  private volatile boolean permanentFailureRecoveryAllowed;

  public PaymentFailureSwitch(
      @Value("${nerv.demo.allow-permanent-failure-recovery:false}") boolean permanentFailureRecoveryAllowed
  ) {
    this.permanentFailureRecoveryAllowed = permanentFailureRecoveryAllowed;
  }

  public boolean permanentFailureRecoveryAllowed() {
    return permanentFailureRecoveryAllowed;
  }

  public void allowPermanentFailureRecovery() {
    permanentFailureRecoveryAllowed = true;
  }
}
