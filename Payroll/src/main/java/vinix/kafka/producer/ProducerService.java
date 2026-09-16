package vinix.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import vinix.kafka.events.PaymentCanceledEvent;
import vinix.kafka.events.PaymentCreatedEvent;
import vinix.kafka.events.PaymentRefundRequestedEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProducerService {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  private static final String PAYMENT_CANCELED_EVENT = "payment-canceled-event";
  private static final String PAYMENT_CREATED_EVENT = "payment-created-event";
  private static final String PAYMENT_REFUND_EVENT = "payment-refund-requested-event";

  private <T> void publishEvent(String topic, String key, T event, String paymentId) {
    log.info("Publicando evento no topico: {} : {}", topic, paymentId);

    kafkaTemplate.send(topic, key, event).whenComplete((res, e) -> {
      if (e != null) {
        log.error("Falha ao publicar evento no topico: {} : {}", topic, paymentId, e);
      } else {
        log.debug("Evento publicado com sucesso no topico: {} : {}", topic, paymentId);
      }
    });
  }

  public void publishCanceledEvent(PaymentCanceledEvent event) {
    publishEvent(PAYMENT_CANCELED_EVENT, event.employeeId().toString(), event, event.paymentId().toString());
  }

  public void publishCreatedEvent(PaymentCreatedEvent event) {
    publishEvent(PAYMENT_CREATED_EVENT, event.employeeId().toString(), event, event.paymentId().toString());
  }

  public void publishRefundRequestedEvent(PaymentRefundRequestedEvent event) {
    publishEvent(PAYMENT_REFUND_EVENT, event.employeeId().toString(), event, event.paymentId().toString());
  }

}
