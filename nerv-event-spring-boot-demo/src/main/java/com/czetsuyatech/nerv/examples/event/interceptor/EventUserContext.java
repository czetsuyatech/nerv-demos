package com.czetsuyatech.nerv.examples.event.interceptor;

/**
 * Simple execution context derived from an event for the demo application.
 */
public record EventUserContext(
    String source,
    String correlationId
) {
}
