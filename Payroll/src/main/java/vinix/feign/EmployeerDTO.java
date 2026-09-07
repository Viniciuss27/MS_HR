package vinix.feign;

import java.math.BigDecimal;

public record EmployeerDTO(
    Long id,
    String name,
    BigDecimal dailyIncome
) {}