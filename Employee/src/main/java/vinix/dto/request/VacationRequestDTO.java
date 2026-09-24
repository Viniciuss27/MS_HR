package vinix.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record VacationRequestDTO(

        @NotNull(message = "Id do Funcionário é obrigatório")
        Long employeeId,

        @NotNull(message = "Data de inicio")
        LocalDate startDate,

        @NotNull(message = "Data final")
        LocalDate endDate
) {}