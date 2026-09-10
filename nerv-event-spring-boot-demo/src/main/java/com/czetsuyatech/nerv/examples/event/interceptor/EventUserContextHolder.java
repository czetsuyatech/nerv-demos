package com.czetsuyatech.nerv.examples.event.interceptor;

/**
 * Holds the demo's current event user context for the processing thread.
 */
public final class EventUserContextHolder {

  private static final ThreadLocal<EventUserContext> CONTEXT = new ThreadLocal<>();

  private EventUserContextHolder() {
  }

  public static EventUserContext get() {
    return CONTEXT.get();
  }

  public static void set(EventUserContext context) {
    CONTEXT.set(context);
  }

  public static void clear() {
    CONTEXT.remove();
  }
}
