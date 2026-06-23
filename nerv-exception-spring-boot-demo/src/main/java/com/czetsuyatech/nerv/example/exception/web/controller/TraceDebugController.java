package com.czetsuyatech.nerv.example.exception.web.controller;

import com.czetsuyatech.nerv.exception.trace.NervTraceContext;
import com.czetsuyatech.nerv.exception.trace.NervTraceContextResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TraceDebugController {

  private final NervTraceContextResolver resolver;

  @GetMapping("/debug/resolver")
  public String resolver() {
    return resolver.getClass().getName();
  }

  @GetMapping("/debug/trace")
  public NervTraceContext trace() {
    return resolver.current();
  }
}
