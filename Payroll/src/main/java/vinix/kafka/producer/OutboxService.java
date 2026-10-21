package vinix.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import vinix.entities.OutboxEvent;
import vinix.kafka.events.producer.PaymentCanceledEvent;
import vinix.kafka.events.producer.PaymentCreatedEvent;
import vinix.kafka.events.producer.PaymentRefundRequestedEvent;
import vinix.repositories.OutboxEventRepository;

@Service
@RequiredArgsConstructor
public class OutboxService {

  private static final String PAYMENT_CANCELED_EVENT = "payment-canceled-event";
  private static final String PAYMENT_CREATED_EVENT = "payment-created-event";
  private static final String PAYMENT_REFUND_EVENT = "payment-refund-requested-event";

  private final OutboxEventRepository repository;
  private final ObjectMapper mapper;

  @Transactional(propagation = Propagation.MANDATORY)
  public void publishCanceledEvent(PaymentCanceledEvent event) {
    save(PAYMENT_CANCELED_EVENT, event.paymentId().toString(), event);
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void publishCreatedEvent(PaymentCreatedEvent event) {
    save(PAYMENT_CREATED_EVENT, event.paymentId().toString(), event);
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void publishRefundRequestedEvent(PaymentRefundRequestedEvent event) {
    save(PAYMENT_REFUND_EVENT, event.paymentId().toString(), event);
  }

  private void save(String topic, String key, Object event) {
    try {
      OutboxEvent row = new OutboxEvent();
      row.setTopic(topic);
      row.setEventKey(key);
      row.setEventType(event.getClass().getName());
      row.setPayload(mapper.writeValueAsString(event));
      repository.save(row);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Falha ao serializar o evento para a outbox", e);
    }
  }

}
