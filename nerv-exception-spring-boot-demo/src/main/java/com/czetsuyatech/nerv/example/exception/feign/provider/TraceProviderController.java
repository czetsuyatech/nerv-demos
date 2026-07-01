package com.czetsuyatech.nerv.example.exception.feign.provider;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TraceProviderController {

  private final Tracer tracer;

  @GetMapping("/provider/trace")
  public Map<String, String> trace() {
    Span span = tracer.currentSpan();

    return Map.of(
        "traceId", span.context().traceId(),
        "spanId", span.context().spanId()
    );
  }
}
