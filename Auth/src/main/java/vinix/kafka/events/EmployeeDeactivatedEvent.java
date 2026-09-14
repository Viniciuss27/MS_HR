package vinix.kafka.events;

import java.time.Instant;

public record EmployeeDeactivatedEvent(
        Long employeeId,
        String employeeName,
        Instant occurredAt
) {}