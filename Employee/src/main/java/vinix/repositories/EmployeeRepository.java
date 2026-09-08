package vinix.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import vinix.entities.Employee;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long>{
  Optional<Employee> findByCpf(String cpf);
  List<Employee> findByActive(boolean active);
}
