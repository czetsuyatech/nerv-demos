package com.czetsuyatech.nerv.example.exception.config;

import com.czetsuyatech.nerv.example.exception.error.PaymentErrorCode;
import com.czetsuyatech.nerv.exception.core.registry.EnumNervErrorCodeRegistry;
import com.czetsuyatech.nerv.exception.core.registry.NervErrorCodeRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RegistryConfiguration {

  @Bean
  NervErrorCodeRegistry paymentErrorCodeRegistry() {
    return new EnumNervErrorCodeRegistry(PaymentErrorCode.values());
  }
}
