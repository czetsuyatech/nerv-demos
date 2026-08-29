package com.czetsuyatech.nerv.examples.event.order;

import com.czetsuyatech.nerv.event.model.Destination;
import com.czetsuyatech.nerv.event.model.EventId;
import com.czetsuyatech.nerv.event.model.EventMessage;
import com.czetsuyatech.nerv.event.model.EventPublication;
import com.czetsuyatech.nerv.event.publisher.EventPublisher;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class OrderService {

  static final String ORDER_CREATED = "order.created";
  private static final String SOURCE = "nerv-event-spring-boot-demo/orders";
  private final OrderRepository orderRepository;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  @Transactional
  public OrderResponse createOrder(String customerId) {
    return create(customerId, false);
  }

  /**
   * Used by the acceptance test to prove one transaction rolls back business and outbox rows.
   */
  @Transactional
  public void createOrderThenFail(String customerId) {
    create(customerId, true);
  }

  private OrderResponse create(String customerId, boolean failAfterPublication) {
    Instant now = clock.instant();
    Order order = orderRepository.save(new Order(UUID.randomUUID(), customerId, "CREATED", now));
    EventId eventId = new EventId(UUID.randomUUID().toString());

    EventMessage<OrderCreated> event = EventMessage.<OrderCreated>builder()
        .id(eventId)
        .type(ORDER_CREATED)
        .timestamp(now)
        .source(SOURCE)
        .correlationId(order.getId().toString())
        .payload(new OrderCreated(order.getId(), order.getCustomerId(), order.getCreatedAt()))
        .build();

    eventPublisher.publish(EventPublication.<OrderCreated>builder()
        .event(event)
        .destination(new Destination("orders-kafka"))
        .build());

    if (failAfterPublication) {
      throw new IllegalStateException("Intentional rollback acceptance-test failure");
    }

    return new OrderResponse(
        order.getId(),
        order.getCustomerId(),
        order.getStatus(),
        order.getCreatedAt(),
        eventId.value()
    );
  }
}
