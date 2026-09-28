package vinix.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import vinix.kafka.events.EmployeeActivatedEvent;
import vinix.kafka.events.EmployeeDeactivatedEvent;
import vinix.kafka.events.EmployeePositionUpdatedEvent;
import vinix.kafka.events.VacationApprovedEvent;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProducerService {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  private static final String VACATION_APPROVED_EVENT = "vacation-approved-event";
  private static final String EMPLOYEE_ACTIVATED_EVENT = "employee-activated-event";
  private static final String EMPLOYEE_DEACTIVATED_EVENT = "employee-deactivated-event";
  private static final String EMPLOYEE_POSITION_UPDATED_EVENT = "employee-position-updated-event";

  private <T> void publish(String topic, String key, T event, String reference) {
    log.info("Publicando evento no tópico {}: {}", topic, reference);

    kafkaTemplate.send(topic, key, event)
        .whenComplete((result, ex) -> {
          if (ex != null) {
            log.error("Falha ao publicar evento no tópico {}: {}", topic, reference, ex);
          } else {
            log.debug("Evento publicado com sucesso no tópico {}: {}", topic, reference);
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

  public void publishVacationApproved(VacationApprovedEvent event) {
    publish(VACATION_APPROVED_EVENT, event.employeeId().toString(), event, event.vacationRequestId().toString());
  }
}
