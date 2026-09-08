package vinix.dto.response;

import java.math.BigDecimal;

public record EmployeeSalaryResponseDTO(// para resposta do feign
    Long id,
    String name,
    BigDecimal dailyIncome
) {}
