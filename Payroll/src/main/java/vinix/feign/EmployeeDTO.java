package vinix.feign;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeDTO(
    Long id,
    String name,
    BigDecimal dailyIncome,
    LocalDate hireDate
) {}