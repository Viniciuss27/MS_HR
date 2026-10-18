package vinix.kafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vinix.entities.OutboxEvent;
import vinix.entities.OutboxStatus;
import vinix.repositories.OutboxEventRepository;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

  private final OutboxEventRepository repository;
  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Scheduled(fixedDelay = 2000)
  public void publishPending() {
    for (OutboxEvent row : repository.findTop50ByStatusOrderByIdAsc(OutboxStatus.PENDING)) {
      try {
        Object event = objectMapper.readValue(row.getPayload(), Class.forName(row.getEventType()));
        kafkaTemplate.send(row.getTopic(), row.getEventKey(), event).get(5, TimeUnit.SECONDS);
        row.setStatus(OutboxStatus.PUBLISHED);
        row.setPublishedAt(Instant.now());
        repository.save(row);
      } catch (Exception e) {
        log.error("Falha ao publicar o evento {} da outbox", row.getId(), e);
        break; // preserva a ordem; tenta de novo no próximo ciclo
      }
    }
  }
}