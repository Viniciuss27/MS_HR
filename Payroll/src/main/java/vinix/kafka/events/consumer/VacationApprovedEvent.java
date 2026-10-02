package vinix.kafka.events.consumer;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record VacationApprovedEvent(
    Long employeeId,
    String employeeName,
    BigDecimal dailyIncome,
    Long vacationRequestId,
    Long decidedBy,
    Instant decidedAt,
    LocalDate startDate,
    LocalDate endDate,
    Integer daysRequested
) {
}