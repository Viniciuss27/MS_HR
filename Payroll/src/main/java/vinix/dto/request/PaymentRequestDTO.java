package vinix.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import vinix.entities.PaymentType;

import java.time.LocalDate;

public record PaymentRequestDTO(
    @NotNull (message = "Employee Id é obrigatório")
    Long employeeId,

    @NotNull (message = "Days Worked é obrigatório")
    @Positive (message = "Somente numeros positivos")
    @Max(value = 31, message = "somente até 31 dias")
    Integer daysWorked,

    @NotNull(message = "Date é obrigatório")
    @PastOrPresent(message = "A data não pode ser futura")
    LocalDate referenceDate
) {}
