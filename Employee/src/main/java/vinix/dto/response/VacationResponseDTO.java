package vinix.dto.response;

import vinix.entities.VacationStatus;

import java.time.Instant;
import java.time.LocalDate;

public record VacationResponseDTO(
        Long id,
        Long employeeId,
        String employeeName,
        LocalDate startDate,
        LocalDate endDate,
        Integer daysRequested,
        VacationStatus status,
        Instant requestedAt,
        Instant decidedAt,
        Long decidedBy,
        LocalDate acquisitionStartDate,
        LocalDate acquisitionEndDate,
        Integer unjustifiedAbsences
) {
}