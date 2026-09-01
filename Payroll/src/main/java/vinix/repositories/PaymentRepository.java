package vinix.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vinix.entities.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
