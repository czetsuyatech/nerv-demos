package com.czetsuyatech.nerv.example.exception.config;

import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignRetryConfiguration {

    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(
            100L,
            300L,
            3
        );
    }
}
