package vinix.kafka.events;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentRefundRequestedEvent(
    Long paymentId,
    Long employeeId,
    BigDecimal amount,
    Instant occurredAt
) {}