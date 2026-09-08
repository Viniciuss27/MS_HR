package vinix.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import org.hibernate.validator.constraints.br.CPF;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeRequestDTO(
    @NotBlank(message = "O nome é obrigatório")
    String name,

    @NotBlank(message = "O CPF é obrigatório")
    @CPF(message = "Digite um CPF válido")
    String cpf,

    @NotBlank(message = "O cargo é obrigatório")
    String position,

    @NotNull(message = "A data de nascimento é obrigatória")
    @Past(message = "A data de nascimento deve estar no passado")
    LocalDate birthDate,

    @NotNull(message = "A renda diária é obrigatória")
    @DecimalMin(value = "1.00", message = "A renda diária deve ser maior que zero")
    BigDecimal dailyIncome
) {}