package vinix.kafka.events;

import java.time.Instant;

public record PaymentCanceledEvent(
    Long paymentId,
    Long employeeId,
    Instant occurredAt
) {}