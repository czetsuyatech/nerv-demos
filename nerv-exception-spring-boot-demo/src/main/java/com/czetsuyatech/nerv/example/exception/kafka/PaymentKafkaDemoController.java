package com.czetsuyatech.nerv.example.exception.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kafka/payments")
public class PaymentKafkaDemoController {

  private final PaymentKafkaProducer producer;

  @PostMapping("/{id}")
  public String publish(@PathVariable String id) {
    producer.publish(new PaymentMessage(id, "PROCESSING"));
    return "Payment message published: " + id;
  }

  @PostMapping("/{id}/fail")
  public String publishFailing(@PathVariable String id) {
    producer.publish(new PaymentMessage(id, "FAIL"));
    return "Failing payment message published: " + id;
  }
}
