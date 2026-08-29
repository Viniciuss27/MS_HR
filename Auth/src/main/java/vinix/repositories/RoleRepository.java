package vinix.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vinix.entities.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Long> {
  Optional<Role> findByRoleName(String name);
}