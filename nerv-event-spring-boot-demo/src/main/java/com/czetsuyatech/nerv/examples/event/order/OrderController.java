package com.czetsuyatech.nerv.examples.event.order;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

  private final OrderService orders;

  public OrderController(OrderService orders) {
    this.orders = orders;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public OrderResponse create(@RequestBody CreateOrderRequest request) {
    return orders.createOrder(request.customerId());
  }
}
