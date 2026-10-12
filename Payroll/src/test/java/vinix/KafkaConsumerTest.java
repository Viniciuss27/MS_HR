package vinix;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vinix.entities.Payment;
import vinix.entities.PaymentStatus;
import vinix.entities.PaymentType;
import vinix.kafka.consumer.KafkaConsumer;
import vinix.kafka.events.consumer.VacationApprovedEvent;
import vinix.repositories.PaymentRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaConsumer do Payroll")
class KafkaConsumerTest {

  @Mock
  private PaymentRepository repository;
  @InjectMocks
  private KafkaConsumer consumer;

  private VacationApprovedEvent event() {
    return new VacationApprovedEvent(
        10L, "João", new BigDecimal("100.00"), 42L, 2L,
        Instant.now(), LocalDate.of(2026, 11, 10),
        LocalDate.of(2026, 11, 19), 10);
  }

  @Test
  @DisplayName("Evento duplicado não gera segundo pagamento")
  void duplicate() {
    when(repository.existsByVacationRequestId(42L)).thenReturn(true);
    consumer.vacationApprovedEvent(event());
    verify(repository, never()).save(any());
  }

  @Test
  @DisplayName("Evento novo gera pagamento de férias com o adicional de 1/3")
  void newEvent() {
    when(repository.existsByVacationRequestId(42L)).thenReturn(false);

    consumer.vacationApprovedEvent(event());

    ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
    verify(repository).save(captor.capture());
    Payment saved = captor.getValue();

    assertEquals(new BigDecimal("1333.33"), saved.getGrossAmount());
    assertEquals(LocalDate.of(2026, 11, 9), saved.getReferenceDate()); // 1 dia antes das férias
    assertEquals(PaymentStatus.PENDING, saved.getStatus());
    assertEquals(PaymentType.VACATION, saved.getType());
    assertEquals(42L, saved.getVacationRequestId());
  }
}