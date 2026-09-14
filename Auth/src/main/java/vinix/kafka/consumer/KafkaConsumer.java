package vinix.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vinix.entities.Role;
import vinix.entities.User;
import vinix.exceptions.ResourceNotFoundException;
import vinix.kafka.events.EmployeeActivatedEvent;
import vinix.kafka.events.EmployeeDeactivatedEvent;
import vinix.kafka.events.EmployeePositionUpdatedEvent;
import vinix.repositories.RoleRepository;
import vinix.repositories.UserRepository;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {

  private final UserRepository repository;
  private final RoleRepository roleRepository;

  @KafkaListener(
      topics = "employee-activated-event",
      groupId = "${spring.kafka.consumer.group-id}"
  )
  @Transactional
  public void consumeEmployeeActivated(EmployeeActivatedEvent event) {
    log.info("Evento recebido: funcionário ativado - {}", event.employeeName());

    User user = verificaEmployeeId(event.employeeId());
    user.setActive(true);
    repository.save(user);
  }

  @KafkaListener(
      topics = "employee-position-updated-event",
      groupId = "${spring.kafka.consumer.group-id}"
  )
  @Transactional
  public void consumeEmployeePositionUpdated(EmployeePositionUpdatedEvent event) {
    log.info("Evento recebido: funcionário atualizado - {}, antigo cargo - {}, novo cargo - {}",
        event.employeeName(), event.oldPosition(), event.newPosition());

    User user = verificaEmployeeId(event.employeeId());
    String roleName = converter(event.newPosition());
    Role role = verificaRoleName(roleName);

    user.getRoles().clear();
    user.getRoles().add(role);
    repository.save(user);
  }

  @KafkaListener(
      topics = "employee-deactivated-event",
      groupId = "${spring.kafka.consumer.group-id}"
  )
  @Transactional
  public void consumeEmployeeDeactivated(EmployeeDeactivatedEvent event) {
    log.info("Evento recebido: funcionário desativado - {}", event.employeeName());

    User user = verificaEmployeeId(event.employeeId());
    user.setActive(false);
    repository.save(user);
  }

  private User verificaEmployeeId(Long employeeId) {
    return repository.findByEmployeeId(employeeId).orElseThrow(() ->
            new ResourceNotFoundException("Funcionário não encontrado para o ID: " + employeeId));
  }

  private Role verificaRoleName(String roleName) {
    return roleRepository.findByRoleName(roleName).orElseThrow(() ->
        new ResourceNotFoundException("Role não encontrada: " + roleName));
  }

  private String converter(String position) {
    return switch (position.toUpperCase(Locale.ROOT)) {
      case "ANALISTA RH" -> "HR";
      case "GERENTE" -> "MANAGER";
      case "ADMINISTRADOR" -> "ADMIN";
      case "COLABORADOR" -> "USER";
      default -> throw new ResourceNotFoundException("Cargo não encontrado: " + position);
    };
  }
}