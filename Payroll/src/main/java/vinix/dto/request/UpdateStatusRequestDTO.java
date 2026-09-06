package vinix.dto.request;

import jakarta.validation.constraints.NotNull;
import vinix.entities.PaymentStatus;

public record UpdateStatusRequestDTO(
    @NotNull(message = "Status é obrigatório")
    PaymentStatus status
) {}