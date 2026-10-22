package vinix;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vinix.entities.OutboxEvent;
import vinix.entities.OutboxStatus;
import vinix.kafka.events.producer.PaymentCanceledEvent;
import vinix.kafka.producer.OutboxService;
import vinix.repositories.OutboxEventRepository;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OutboxServiceTest {

  @Mock
  private OutboxEventRepository repository;
  private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
  private OutboxService service;

  @BeforeEach
  void setUp() { service = new OutboxService(repository, mapper); }

  @Test
  @DisplayName("Grava o evento como PENDING, com tópico, chave, tipo e JSON")
  void save() {
    service.saveCanceledEvent(new PaymentCanceledEvent(7L, 10L, Instant.parse("2026-10-01T10:00:00Z")));

    ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
    verify(repository).save(captor.capture());
    OutboxEvent row = captor.getValue();

    assertEquals("payment-canceled-event", row.getTopic());
    assertEquals("10", row.getEventKey());
    assertEquals(PaymentCanceledEvent.class.getName(), row.getEventType());
    assertEquals(OutboxStatus.PENDING, row.getStatus());
    assertTrue(row.getPayload().contains("\"paymentId\":7"));
  }
}