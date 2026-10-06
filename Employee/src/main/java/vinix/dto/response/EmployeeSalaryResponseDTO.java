package vinix.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeSalaryResponseDTO(// para resposta do feign
    Long id,
    String name,
    BigDecimal dailyIncome,
    LocalDate hireDate
) {}
