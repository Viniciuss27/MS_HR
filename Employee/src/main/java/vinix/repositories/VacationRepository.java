package vinix.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vinix.entities.Employee;
import vinix.entities.VacationRequest;
import vinix.entities.VacationStatus;

import java.time.LocalDate;
import java.util.List;

public interface VacationRepository extends JpaRepository<VacationRequest, Long> {
  List<VacationRequest> findByEmployeeAndAcquisitionStartDateAndStatusIn(
      Employee employee, LocalDate acquisitionStartDate, List<VacationStatus> statuses);
}
