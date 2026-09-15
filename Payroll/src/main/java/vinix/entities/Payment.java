package vinix.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "tb_payment")
public class Payment implements Serializable {
		private static final long serialVersionUID = 1L;

		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		private Long id;

		@Column(nullable = false, name = "employee_id")
		private Long employeeId;

		@Column(nullable = false, name = "employee_name")
		private String employeeName;

		@Column(nullable = false, name = "daily_income", precision = 19, scale = 2)
		private BigDecimal dailyIncome;

		@Column(nullable = false, name = "days_worked")
		private Integer daysWorked;

		@Column(nullable = false, name = "gross_amount", precision = 19, scale = 2)
		private BigDecimal grossAmount;

		@Column(name = "payment_date")
		private Instant paymentDate;

		@Column(nullable = false, name = "reference_date")
		private LocalDate referenceDate;

		@Column(nullable = false)
		@Enumerated(EnumType.STRING)
		private PaymentStatus status;

		@Column(nullable = false)
		@Enumerated(EnumType.STRING)
		private PaymentType type;

		@Column(nullable = false, updatable = false)
		@CreationTimestamp
		private Instant createdAt;

		@Column(nullable = false)
		@UpdateTimestamp
		private Instant updatedAt;
}
