package vinix.dto.response;

import vinix.entities.PaymentStatus;
import vinix.entities.PaymentType;

import java.math.BigDecimal;

import java.time.LocalDate;

public record PaymentResponseDTO(
    Long id,
    Long workerId,
    String workerName,
    BigDecimal grossAmount,
    PaymentStatus status,
    PaymentType type,
    LocalDate referenceDate
) {}
