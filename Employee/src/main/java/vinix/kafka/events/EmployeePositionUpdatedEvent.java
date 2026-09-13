package vinix.kafka.events;

import java.time.Instant;

public record EmployeePositionUpdatedEvent(
        Long employeeId,
        String employeeName,
        String oldPosition,
        String newPosition,
        Instant occurredAt
) {}