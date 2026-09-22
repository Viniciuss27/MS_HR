package vinix.dto.response;

import java.time.Instant;
import java.time.LocalDate;

public record EmployeeDetailsResponseDTO(// para HR
    Long id,
    String name,
    String cpf,
    String position,
    LocalDate birthDate,
    LocalDate hireDate,
    Instant createdAt
) {}