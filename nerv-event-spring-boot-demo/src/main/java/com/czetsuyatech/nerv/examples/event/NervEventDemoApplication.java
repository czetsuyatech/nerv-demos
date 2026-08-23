package com.czetsuyatech.nerv.examples.event;

import com.czetsuyatech.nerv.event.core.retry.RetryPolicy;
import java.time.Duration;
import java.time.Instant;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackageClasses = NervEventDemoApplication.class)
public class NervEventDemoApplication {

  @Bean
  RetryPolicy outboxRetryPolicy() {
    return new RetryPolicy() {
      @Override
      public boolean allowsRetry(int failedAttemptCount) {
        return failedAttemptCount < 5;
      }

      @Override
      public Instant nextEligibleAt(int failedAttemptCount, Instant failedAt) {
        return failedAt.plus(Duration.ofSeconds(Math.min(30, 1L << (failedAttemptCount - 1))));
      }
    };
  }

  public static void main(String[] args) {
    SpringApplication.run(NervEventDemoApplication.class, args);
  }
}
