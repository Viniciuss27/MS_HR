package vinix.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import vinix.entities.Worker;

public interface WorkerRepository extends JpaRepository<Worker, Long>{

}
