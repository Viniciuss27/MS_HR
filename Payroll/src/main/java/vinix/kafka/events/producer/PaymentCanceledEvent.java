package vinix.kafka.events.producer;

import java.time.Instant;

public record PaymentCanceledEvent(
    Long paymentId,
    Long employeeId,
    Instant occurredAt
) {}