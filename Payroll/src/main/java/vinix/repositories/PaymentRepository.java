package vinix.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vinix.entities.Payment;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
  List<Payment> findByEmployeeId(Long employeeId);
}
