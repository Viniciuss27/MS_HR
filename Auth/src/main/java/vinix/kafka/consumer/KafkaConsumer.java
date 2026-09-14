package vinix.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vinix.entities.User;
import vinix.exceptions.ResourceNotFoundException;
import vinix.kafka.events.EmployeeActivatedEvent;
import vinix.kafka.events.EmployeeDeactivatedEvent;
import vinix.kafka.events.EmployeePositionUpdatedEvent;
import vinix.repositories.UserRepository;
import vinix.services.AuthService;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {

  private final AuthService service;
  private final UserRepository repository;

  @KafkaListener(
      topics = "employee-activated-event",
      groupId = "${spring.kafka.consumer.group-id}")
  @Transactional
  public void consumeEmployeeActivated(EmployeeActivatedEvent event) {
    log.info("Evento recebido: funcionário ativado - {}", event.employeeName());

    User user = repository.findByUsername(event.employeeName()).orElseThrow(() -> new ResourceNotFoundException("Funcionario com esse nome não encontrado"));

  }
}
