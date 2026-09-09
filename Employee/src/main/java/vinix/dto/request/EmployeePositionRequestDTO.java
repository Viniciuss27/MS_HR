package vinix.dto.request;

import jakarta.validation.constraints.NotBlank;

public record EmployeePositionRequestDTO(
    @NotBlank(message = "O cargo é obrigatório")
    String position
) {}