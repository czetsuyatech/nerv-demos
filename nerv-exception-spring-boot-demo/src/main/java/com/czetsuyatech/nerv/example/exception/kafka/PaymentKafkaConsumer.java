package com.czetsuyatech.nerv.example.exception.kafka;

import com.czetsuyatech.nerv.example.exception.error.PaymentErrorCode;
import com.czetsuyatech.nerv.exception.core.NervException;
import com.czetsuyatech.nerv.exception.event.NervErrorEvent;
import com.czetsuyatech.nerv.exception.kafka.NervKafkaErrorHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentKafkaConsumer {

  private final NervKafkaErrorHandler errorHandler;

  @KafkaListener(
      topics = "payments",
      groupId = "nerv-exception-demo"
  )
  public void consume(
      PaymentMessage message,
      ConsumerRecord<String, PaymentMessage> record
  ) {
    try {
      if ("FAIL".equals(message.status())) {
        throw NervException.of(PaymentErrorCode.PAYMENT_TIMEOUT);
      }

      log.debug("Payment processed: {}", message.paymentId());
    } catch (Exception ex) {
      errorHandler.handle(record, ex);
    }
  }

  @KafkaListener(
      topics = "payments.dlq",
      groupId = "nerv-exception-demo-dlq"
  )
  public void consumeDlq(
      NervErrorEvent event,
      ConsumerRecord<String, NervErrorEvent> record
  ) {
    log.info("DLQ event received:");
    log.debug("{}", event);

    record.headers().forEach(header ->
        log.info(header.key() + "=" + new String(header.value()))
    );
  }
}
