package vinix.feign;

import java.math.BigDecimal;

public record EmployeeDTO(
    Long id,
    String name,
    BigDecimal dailyIncome
) {}