package com.czetsuyatech.nerv.example.exception.config;

import com.czetsuyatech.nerv.exception.web.NervTraceContextResolver;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DemoTraceConfiguration {

  private final Tracer tracer;

  @Bean
  NervTraceContextResolver nervTraceContextResolver() {
    return new NervTraceContextResolver() {

      @Override
      public String traceId() {
        var span = tracer.currentSpan();
        return span == null ? null : span.context().traceId();
      }

      @Override
      public String spanId() {
        var span = tracer.currentSpan();
        return span == null ? null : span.context().spanId();
      }
    };
  }
}
