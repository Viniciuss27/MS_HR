package vinix.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vinix.entities.Payment;
import vinix.entities.PaymentType;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
  List<Payment> findByEmployeeId(Long employeeId);
  List<Payment> findByType(PaymentType type);
  Optional<Payment> findByVacationRequestId(Long vacationRequestId);
  boolean existsByVacationRequestId(Long vacationRequestId);
}
