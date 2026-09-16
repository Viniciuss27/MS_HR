package vinix.kafka.events;

import vinix.entities.PaymentType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record PaymentCreatedEvent(
    Long paymentId,
    Long employeeId,
    String employeeName,
    BigDecimal grossAmount,
    PaymentType type,
    LocalDate referenceDate,
    Instant occurredAt
) {}