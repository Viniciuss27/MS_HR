package vinix.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import vinix.kafka.events.EmployeeActivatedEvent;
import vinix.kafka.events.EmployeeDeactivatedEvent;
import vinix.kafka.events.EmployeePositionUpdatedEvent;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProducerService {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  private static final String EMPLOYEE_ACTIVATED_EVENT = "employee-activated-event";
  private static final String EMPLOYEE_DEACTIVATED_EVENT = "employee-deactivated-event";
  private static final String EMPLOYEE_POSITION_UPDATED_EVENT = "employee-position-updated-event";

  private <T> void publish(String topic, String key, T event, String employeeName) {
    log.info("Publicando evento no tópico {}: {}", topic, employeeName);

    kafkaTemplate.send(topic, key, event)
        .whenComplete((result, ex) -> {
          if (ex != null) {
            log.error("Falha ao publicar evento no tópico {}: {}", topic, employeeName, ex);
          } else {
            log.debug("Evento publicado com sucesso no tópico {}: {}", topic, employeeName);
          }
        });
  }

  public void publishEmployeeActivated(EmployeeActivatedEvent event) {
    publish(EMPLOYEE_ACTIVATED_EVENT, event.employeeId().toString(), event, event.employeeName());
  }

  public void publishEmployeeDeactivated(EmployeeDeactivatedEvent event) {
    publish(EMPLOYEE_DEACTIVATED_EVENT, event.employeeId().toString(), event, event.employeeName());
  }

  public void publishEmployeePositionUpdated(EmployeePositionUpdatedEvent event) {
    publish(EMPLOYEE_POSITION_UPDATED_EVENT, event.employeeId().toString(), event, event.employeeName());
  }
}
