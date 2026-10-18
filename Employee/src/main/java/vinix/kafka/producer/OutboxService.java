package vinix.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import vinix.entities.OutboxEvent;
import vinix.kafka.events.EmployeeActivatedEvent;
import vinix.kafka.events.EmployeeDeactivatedEvent;
import vinix.kafka.events.EmployeePositionUpdatedEvent;
import vinix.kafka.events.VacationApprovedEvent;
import vinix.repositories.OutboxEventRepository;

@Service
@RequiredArgsConstructor
public class OutboxService {

  private static final String EMPLOYEE_ACTIVATED_EVENT = "employee-activated-event";
  private static final String EMPLOYEE_DEACTIVATED_EVENT = "employee-deactivated-event";
  private static final String EMPLOYEE_POSITION_UPDATED_EVENT = "employee-position-updated-event";
  private static final String VACATION_APPROVED_EVENT = "vacation-approved-event";

  private final OutboxEventRepository repository;
  private final ObjectMapper objectMapper;

  @Transactional(propagation = Propagation.MANDATORY)
  public void saveEmployeeActivated(EmployeeActivatedEvent event) {
    save(EMPLOYEE_ACTIVATED_EVENT, event.employeeId().toString(), event);
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void saveEmployeeDeactivated(EmployeeDeactivatedEvent event) {
    save(EMPLOYEE_DEACTIVATED_EVENT, event.employeeId().toString(), event);
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void saveEmployeePositionUpdated(EmployeePositionUpdatedEvent event) {
    save(EMPLOYEE_POSITION_UPDATED_EVENT, event.employeeId().toString(), event);
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void saveVacationApproved(VacationApprovedEvent event) {
    save(VACATION_APPROVED_EVENT, event.employeeId().toString(), event);
  }

  private void save(String topic, String key, Object event) {
    try {
      OutboxEvent row = new OutboxEvent();
      row.setTopic(topic);
      row.setEventKey(key);
      row.setEventType(event.getClass().getName());
      row.setPayload(objectMapper.writeValueAsString(event));
      repository.save(row);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Falha ao serializar o evento para a outbox", e);
    }
  }
}

