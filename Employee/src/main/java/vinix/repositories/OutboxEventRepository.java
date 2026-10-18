package vinix.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vinix.entities.OutboxEvent;
import vinix.entities.OutboxStatus;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
  List<OutboxEvent> findTop50ByStatusOrderByIdAsc(OutboxStatus status);
}