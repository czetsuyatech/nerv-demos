package com.czetsuyatech.nerv.examples.event.order;

import com.czetsuyatech.nerv.event.consumer.EventHandler;
import com.czetsuyatech.nerv.event.model.Destination;
import com.czetsuyatech.nerv.event.model.EventId;
import com.czetsuyatech.nerv.event.model.EventMessage;
import com.czetsuyatech.nerv.event.model.EventPublication;
import com.czetsuyatech.nerv.event.publisher.EventPublisher;
import com.czetsuyatech.nerv.examples.event.payment.PaymentRequested;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Kafka consumer handler which starts the SQS payment flow using only public NERV types. */
@Component
public class OrderCreatedHandler implements EventHandler<OrderCreated> {

  private final EventPublisher eventPublisher;
  private final Clock clock;

  public OrderCreatedHandler(EventPublisher eventPublisher, Clock clock) {
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  public String eventType() {
    return OrderService.ORDER_CREATED;
  }

  @Override
  public Class<OrderCreated> payloadType() {
    return OrderCreated.class;
  }

  @Override
  public void handle(EventMessage<OrderCreated> event) {
    OrderCreated order = event.payload();
    EventMessage<PaymentRequested> payment = EventMessage.<PaymentRequested>builder()
        .id(new EventId(UUID.randomUUID().toString()))
        .type("payment.requested")
        .timestamp(clock.instant())
        .source("nerv-event-spring-boot-demo/payments")
        .correlationId(event.correlationId())
        .payload(new PaymentRequested(order.orderId(), order.customerId(), clock.instant()))
        .build();
    eventPublisher.publish(EventPublication.<PaymentRequested>builder()
        .event(payment)
        .destination(new Destination("payments-sqs"))
        .build());
  }
}
