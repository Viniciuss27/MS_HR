package vinix.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vinix.entities.VacationRequest;

public interface VacationRepository extends JpaRepository<VacationRequest, Long> {
}
