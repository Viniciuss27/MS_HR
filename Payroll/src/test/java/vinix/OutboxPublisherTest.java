package vinix;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import vinix.entities.OutboxEvent;
import vinix.entities.OutboxStatus;
import vinix.kafka.events.producer.PaymentCanceledEvent;
import vinix.kafka.producer.OutboxPublisher;
import vinix.repositories.OutboxEventRepository;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherTest {

  @Mock
  private OutboxEventRepository repository;
  @Mock private KafkaTemplate<String, Object> kafkaTemplate;
  private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
  private OutboxPublisher publisher;

  @BeforeEach
  void setUp() { publisher = new OutboxPublisher(repository, kafkaTemplate, mapper); }

  private OutboxEvent row(Long id) throws Exception {
    OutboxEvent row = new OutboxEvent();
    row.setId(id);
    row.setTopic("payment-canceled-event");
    row.setEventKey("10");
    row.setEventType(PaymentCanceledEvent.class.getName());
    row.setPayload(mapper.writeValueAsString(new PaymentCanceledEvent(id, 10L, Instant.now())));
    return row;
  }

  @Test
  @DisplayName("Publica e marca como PUBLISHED")
  void publishes() throws Exception {
    OutboxEvent row = row(1L);
    when(repository.findTop50ByStatusOrderByIdAsc(OutboxStatus.PENDING)).thenReturn(List.of(row));
    when(kafkaTemplate.send(eq("payment-canceled-event"), eq("10"), any(PaymentCanceledEvent.class)))
        .thenReturn(CompletableFuture.completedFuture(null));

    publisher.publishPending();

    assertEquals(OutboxStatus.PUBLISHED, row.getStatus());
    assertNotNull(row.getPublishedAt());
    verify(repository).save(row);
  }

  @Test
  @DisplayName("Se o Kafka falha, mantém PENDING e para, preservando a ordem")
  void stopsOnFailure() throws Exception {
    OutboxEvent first = row(1L);
    OutboxEvent second = row(2L);
    when(repository.findTop50ByStatusOrderByIdAsc(OutboxStatus.PENDING)).thenReturn(List.of(first, second));
    when(kafkaTemplate.send(anyString(), anyString(), any()))
        .thenReturn(CompletableFuture.failedFuture(new RuntimeException("kafka fora")));

    publisher.publishPending();

    assertEquals(OutboxStatus.PENDING, first.getStatus());
    assertEquals(OutboxStatus.PENDING, second.getStatus());
    verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any());
    verify(repository, never()).save(any());
  }
}