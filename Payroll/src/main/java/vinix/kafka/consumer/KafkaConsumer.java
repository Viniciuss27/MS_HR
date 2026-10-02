package vinix.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vinix.entities.Payment;
import vinix.entities.PaymentStatus;
import vinix.entities.PaymentType;
import vinix.exceptions.ResourceNotFoundException;
import vinix.kafka.events.consumer.VacationApprovedEvent;
import vinix.repositories.PaymentRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {

  private final PaymentRepository repository;

  @KafkaListener(
      topics = "vacation-approved-event",
      groupId = "${spring.kafka.consumer.group-id}")
  @Transactional
  public void vacationApprovedEvent(VacationApprovedEvent event) {
    if (repository.existsByVacationRequestId(event.vacationRequestId())) {
      log.warn("Pagamento de férias já existe para a solicitação {}", event.vacationRequestId());
      return;
    }
    log.info("Evento recebido: férias aprovadas por {}, para {}", event.decidedBy(), event.employeeName());

    BigDecimal grossAmount = event.dailyIncome()
        .multiply(BigDecimal.valueOf(event.daysRequested()))
        .multiply(BigDecimal.valueOf(4))
        .divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);

    Payment payment = Payment.builder()
        .employeeId(event.employeeId())
        .employeeName(event.employeeName())
        .dailyIncome(event.dailyIncome())
        .daysWorked(event.daysRequested())
        .vacationRequestId(event.vacationRequestId())
        .status(PaymentStatus.PENDING)
        .type(PaymentType.VACATION)
        .grossAmount(grossAmount)
        .referenceDate(event.startDate().minusDays(1)).build();

    repository.save(payment);
  }
}
