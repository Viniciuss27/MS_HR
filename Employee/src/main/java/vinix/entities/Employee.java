package vinix.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Getter @Setter @Builder
@Entity @Table(name = "tb_employee")
public class Employee implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, unique = true)
	private String cpf;

	@Column(nullable = false)
	private String position;

	@Column(nullable = false, name = "birth_date")
	private LocalDate birthDate;

	@Column(nullable = false, name = "hire_date", updatable = false)
	private LocalDate hireDate;

	@Column(nullable = false, name = "daily_income")
	private BigDecimal dailyIncome;

	@Column(nullable = false)
	private Boolean active;
}
