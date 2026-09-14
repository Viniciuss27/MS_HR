package vinix.kafka.events;

import java.time.Instant;

public record EmployeeActivatedEvent(
        Long employeeId,
        String employeeName,
        Instant occurredAt
) {}