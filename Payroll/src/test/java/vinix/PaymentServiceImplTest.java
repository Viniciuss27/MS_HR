package vinix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import vinix.dto.request.PaymentRequestDTO;
import vinix.dto.response.PaymentResponseDTO;
import vinix.entities.Payment;
import vinix.entities.PaymentStatus;
import vinix.entities.PaymentType;
import vinix.feign.EmployeeDTO;
import vinix.feign.EmployeeFeignClient;
import vinix.kafka.events.PaymentCanceledEvent;
import vinix.kafka.events.PaymentCreatedEvent;
import vinix.kafka.events.PaymentRefundRequestedEvent;
import vinix.kafka.producer.ProducerService;
import vinix.mapper.PaymentMapper;
import vinix.repositories.PaymentRepository;
import vinix.services.PaymentServiceImpl;
import vinix.services.exceptions.ResourceNotFoundException;
import vinix.services.exceptions.ServicoIndisponivelException;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentServiceImplTest")
class PaymentServiceImplTest {

  @Mock private EmployeeFeignClient feign;
  @Mock private PaymentMapper mapper;
  @Mock private PaymentRepository repository;
  @Mock private ProducerService kafka;

  @InjectMocks
  private PaymentServiceImpl service;

  @Test // FindAll
  @DisplayName("Deve retornar todos os pagamentos")
  void findAll() {

    Payment payment = Payment.builder().id(1L).employeeId(10L)
        .employeeName("João").grossAmount(new BigDecimal("3000.00")).build();

    PaymentResponseDTO dto = new PaymentResponseDTO(1L,10L,"João",
        new BigDecimal("3000.00"), PaymentStatus.PENDING, PaymentType.SALARY, LocalDate.now());

    when(repository.findAll()).thenReturn(List.of(payment));
    when(mapper.toDTO(payment)).thenReturn(dto);

    List<PaymentResponseDTO> result = service.findAll();

    assertEquals(1, result.size());
    assertEquals(dto, result.get(0));

    verify(repository).findAll();
    verify(mapper).toDTO(payment);
  }


  @Test // FindById
  @DisplayName("Deve encontrar pagamento pelo ID")
  void findById() {

    Payment payment = Payment.builder().id(1L).employeeId(10L).employeeName("João").build();

    PaymentResponseDTO dto = new PaymentResponseDTO(1L, 10L, "João",
        BigDecimal.ZERO, PaymentStatus.PENDING, PaymentType.SALARY, LocalDate.now());

    when(repository.findById(1L)).thenReturn(Optional.of(payment));
    when(mapper.toDTO(payment)).thenReturn(dto);

    PaymentResponseDTO result = service.findById(1L);

    assertEquals(dto, result);

    verify(repository).findById(1L);
    verify(mapper).toDTO(payment);
  }


  @Test
  @DisplayName("Deve lançar exceção quando pagamento não existir")
  void findByIdNotFound() {

    when(repository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> service.findById(1L));

    verify(repository).findById(1L);
    verify(mapper, never()).toDTO(any());
  }

  @Test // FindByWorkerId
  @DisplayName("Deve retornar pagamentos do funcionário")
  void findByWorkerId() {

    EmployeeDTO worker = new EmployeeDTO(10L, "João", new BigDecimal("100.00"));

    Payment payment = Payment.builder().id(1L).employeeId(10L).employeeName("João").build();

    PaymentResponseDTO dto = new PaymentResponseDTO(1L, 10L, "João", BigDecimal.ZERO,
        PaymentStatus.PENDING, PaymentType.SALARY, LocalDate.now());

    when(feign.findById(10L)).thenReturn(ResponseEntity.ok(worker));
    when(repository.findByEmployeeId(10L)).thenReturn(List.of(payment));
    when(mapper.toDTO(payment)).thenReturn(dto);

    List<PaymentResponseDTO> result = service.findByEmployeeId(10L);

    assertEquals(1, result.size());
    assertEquals(dto, result.get(0));

    verify(feign).findById(10L);
    verify(repository).findByEmployeeId(10L);
    verify(mapper).toDTO(payment);
  }


  @Test
  @DisplayName("Deve lançar exceção quando funcionário não existir")
  void findByWorkerIdNotFound() {

    when(feign.findById(10L)).thenReturn(ResponseEntity.notFound().build());

    assertThrows(ResourceNotFoundException.class,() -> service.findByEmployeeId(10L));

    verify(feign).findById(10L);
    verify(repository, never()).findByEmployeeId(anyLong());
  }


  @Test
  @DisplayName("Deve lançar exceção quando serviço de funcionário estiver indisponível")
  void findByWorkerIdServiceUnavailable() {

    when(feign.findById(10L)).thenReturn(ResponseEntity.status(SERVICE_UNAVAILABLE).build());

    assertThrows(ServicoIndisponivelException.class, () -> service.findByEmployeeId(10L));

    verify(feign).findById(10L);
    verify(repository, never()).findByEmployeeId(anyLong());
  }

  @Test //Create
  @DisplayName("Deve criar um pagamento salarial")
  void create() {

    PaymentRequestDTO request = new PaymentRequestDTO(10L, 30, LocalDate.of(2026, 9, 1));

    EmployeeDTO worker = new EmployeeDTO(10L, "João", new BigDecimal("100.00"));

    Payment savedPayment = Payment.builder().id(1L).employeeId(10L).employeeName("João")
        .dailyIncome(new BigDecimal("100.00"))
        .daysWorked(30).grossAmount(new BigDecimal("3000.00")).referenceDate(request.referenceDate())
        .status(PaymentStatus.PENDING).type(PaymentType.SALARY).build();

    PaymentResponseDTO response = new PaymentResponseDTO(1L, 10L, "João",
        new BigDecimal("3000.00"), PaymentStatus.PENDING, PaymentType.SALARY, request.referenceDate());

    when(feign.findById(10L)).thenReturn(ResponseEntity.ok(worker));
    when(repository.save(any(Payment.class))).thenReturn(savedPayment);
    when(mapper.toDTO(savedPayment)).thenReturn(response);

    PaymentResponseDTO result = service.create(request);

    assertEquals(response, result);

    verify(feign).findById(10L);
    verify(repository).save(any(Payment.class));
    verify(kafka).publishCreatedEvent(any(PaymentCreatedEvent.class));
    verify(mapper).toDTO(savedPayment);
  }

  @Test // 13° salario
  @DisplayName("Deve calcular e criar pagamento de 13º salário")
  void calculate13Salary() {

    PaymentRequestDTO request = new PaymentRequestDTO(10L, 30, LocalDate.of(2026, 12, 1));

    EmployeeDTO worker = new EmployeeDTO(10L, "João", new BigDecimal("100.00"));

    Payment savedPayment = Payment.builder().id(2L).employeeId(10L).employeeName("João")
        .dailyIncome(new BigDecimal("100.00"))
        .daysWorked(30).grossAmount(new BigDecimal("3000.00")).referenceDate(request.referenceDate())
        .status(PaymentStatus.PENDING).type(PaymentType.THIRTEENTH).build();

    PaymentResponseDTO response = new PaymentResponseDTO(2L, 10L, "João", new BigDecimal("3000.00"),
        PaymentStatus.PENDING, PaymentType.THIRTEENTH, request.referenceDate());

    when(feign.findById(10L)).thenReturn(ResponseEntity.ok(worker));
    when(repository.save(any(Payment.class))).thenReturn(savedPayment);
    when(mapper.toDTO(savedPayment)).thenReturn(response);

    PaymentResponseDTO result = service.calculate13Salary(request);

    assertEquals(response, result);

    verify(feign).findById(10L);
    verify(repository).save(any(Payment.class));
    verify(kafka).publishCreatedEvent(any(PaymentCreatedEvent.class));
    verify(mapper).toDTO(savedPayment);
  }

  @Test // Férias
  @DisplayName("Deve calcular e criar pagamento de férias")
  void vacation() {

    PaymentRequestDTO request = new PaymentRequestDTO(10L, 30, LocalDate.of(2026, 10, 1));

    EmployeeDTO worker = new EmployeeDTO(10L, "João", new BigDecimal("100.00"));

    Payment savedPayment = Payment.builder().id(3L).employeeId(10L).employeeName("João")
        .dailyIncome(new BigDecimal("100.00"))
        .daysWorked(30).grossAmount(new BigDecimal("3000.00")).referenceDate(request.referenceDate())
        .status(PaymentStatus.PENDING).type(PaymentType.VACATION).build();

    PaymentResponseDTO response = new PaymentResponseDTO(3L, 10L, "João",
        new BigDecimal("3000.00"), PaymentStatus.PENDING, PaymentType.VACATION, request.referenceDate());

    when(feign.findById(10L)).thenReturn(ResponseEntity.ok(worker));
    when(repository.save(any(Payment.class))).thenReturn(savedPayment);
    when(mapper.toDTO(savedPayment)).thenReturn(response);

    PaymentResponseDTO result = service.calculateVacation(request);

    assertEquals(response, result);

    verify(feign).findById(10L);
    verify(repository).save(any(Payment.class));
    verify(kafka).publishCreatedEvent(any(PaymentCreatedEvent.class));
    verify(mapper).toDTO(savedPayment);
  }


  @Test // Update Status
  @DisplayName("Deve atualizar o status do pagamento")
  void updateStatus() {

    Payment payment = Payment.builder().id(1L).status(PaymentStatus.PENDING).build();

    PaymentResponseDTO response = new PaymentResponseDTO(1L, 10L, "João",
        BigDecimal.ZERO, PaymentStatus.PAID, PaymentType.SALARY, LocalDate.now());

    when(repository.findById(1L)).thenReturn(Optional.of(payment));
    when(repository.save(payment)).thenReturn(payment);
    when(mapper.toDTO(payment)).thenReturn(response);

    PaymentResponseDTO result = service.updateStatus(1L, PaymentStatus.PAID);

    assertEquals(PaymentStatus.PAID, payment.getStatus());
    assertEquals(response, result);

    verify(repository).findById(1L);
    verify(repository).save(payment);
    verify(mapper).toDTO(payment);
  }


  @Test
  @DisplayName("Não deve permitir alterar status diretamente para CANCELED")
  void updateStatusCanceled() {

    assertThrows(IllegalArgumentException.class, () -> service.updateStatus(1L, PaymentStatus.CANCELED));

    verify(repository, never()).findById(anyLong());
    verify(repository, never()).save(any());
  }


  @Test // Cancel
  @DisplayName("Deve cancelar pagamento PENDING")
  void cancel() {

    Payment payment = Payment.builder().id(1L).status(PaymentStatus.PENDING).build();

    PaymentResponseDTO response = new PaymentResponseDTO(1L, 10L, "João",
        BigDecimal.ZERO, PaymentStatus.CANCELED, PaymentType.SALARY, LocalDate.now());

    when(repository.findById(1L)).thenReturn(Optional.of(payment));
    when(repository.save(payment)).thenReturn(payment);
    when(mapper.toDTO(payment)).thenReturn(response);

    PaymentResponseDTO result = service.cancel(1L);

    assertEquals(PaymentStatus.CANCELED, payment.getStatus());
    assertEquals(response, result);

    verify(repository).findById(1L);
    verify(repository).save(payment);
    verify(kafka).publishCanceledEvent(any(PaymentCanceledEvent.class));
    verify(mapper).toDTO(payment);
  }


  @Test
  @DisplayName("Deve cancelar pagamento PAID")
  void cancelPaid() {

    Payment payment = Payment.builder().id(1L).status(PaymentStatus.PAID).build();

    PaymentResponseDTO response = new PaymentResponseDTO(1L, 10L, "João",
        BigDecimal.ZERO, PaymentStatus.CANCELED, PaymentType.SALARY, LocalDate.now());

    when(repository.findById(1L)).thenReturn(Optional.of(payment));
    when(repository.save(payment)).thenReturn(payment);
    when(mapper.toDTO(payment)).thenReturn(response);

    PaymentResponseDTO result = service.cancel(1L);

    assertEquals(PaymentStatus.CANCELED, payment.getStatus());
    assertEquals(response, result);

    verify(repository).findById(1L);
    verify(repository).save(payment);
    verify(kafka).publishRefundRequestedEvent(any(PaymentRefundRequestedEvent.class));
    verify(mapper).toDTO(payment);
  }


  @Test
  @DisplayName("Não deve cancelar pagamento com status inválido")
  void cancelInvalidStatus() {

    Payment payment = Payment.builder().id(1L).status(PaymentStatus.FAILED).build();

    when(repository.findById(1L)).thenReturn(Optional.of(payment));

    assertThrows(IllegalStateException.class, () -> service.cancel(1L));

    verify(repository).findById(1L);
    verify(repository, never()).save(any());
  }


  @Test
  @DisplayName("Não deve cancelar pagamento já cancelado")
  void cancelNot() {

    Payment payment = Payment.builder().id(1L).status(PaymentStatus.CANCELED).build();

    when(repository.findById(1L)).thenReturn(Optional.of(payment));

    PaymentResponseDTO response = new PaymentResponseDTO(1L, 10L, "João",
        BigDecimal.ZERO, PaymentStatus.CANCELED, PaymentType.SALARY, LocalDate.now());

    when(mapper.toDTO(payment)).thenReturn(response);

    PaymentResponseDTO result = service.cancel(1L);

    assertEquals(PaymentStatus.CANCELED, payment.getStatus());
    assertEquals(response, result);

    verify(repository).findById(1L);
    verify(repository, never()).save(any());
    verify(mapper).toDTO(payment);
  }


  @Test
  @DisplayName("Deve lançar exceção ao cancelar pagamento inexistente")
  void cancelNotFound() {

    when(repository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> service.cancel(1L));

    verify(repository).findById(1L);
    verify(repository, never()).save(any());
  }

  @Test// Launch Payroll
  @DisplayName("Deve lançar a folha de pagamento para todos os funcionários")
  void launchPayroll() {

    EmployeeDTO worker1 = new EmployeeDTO(1L, "João", new BigDecimal("100.00"));
    EmployeeDTO worker2 = new EmployeeDTO(2L, "Maria", new BigDecimal("150.00"));

    Payment payment1 = Payment.builder().id(1L).employeeId(1L).employeeName("João")
        .grossAmount(new BigDecimal("3000.00")).status(PaymentStatus.PENDING).type(PaymentType.SALARY).build();

    Payment payment2 = Payment.builder().id(2L).employeeId(2L).employeeName("Maria")
        .grossAmount(new BigDecimal("4500.00")).status(PaymentStatus.PENDING).type(PaymentType.SALARY).build();

    PaymentResponseDTO dto1 = new PaymentResponseDTO(1L, 1L, "João",
        new BigDecimal("3000.00"), PaymentStatus.PENDING, PaymentType.SALARY, LocalDate.now());

    PaymentResponseDTO dto2 = new PaymentResponseDTO(2L, 2L, "Maria",
        new BigDecimal("4500.00"), PaymentStatus.PENDING, PaymentType.SALARY, LocalDate.now());

    when(feign.findAllActive()).thenReturn(ResponseEntity.ok(List.of(worker1, worker2)));
    when(repository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(mapper.toDTO(any(Payment.class))).thenReturn(dto1, dto2);

    List<PaymentResponseDTO> result = service.launchPayroll();

    assertEquals(2, result.size());
    assertEquals(dto1, result.get(0));
    assertEquals(dto2, result.get(1));

    verify(feign).findAllActive();
    verify(repository, times(2)).save(any(Payment.class));
    verify(mapper, times(2)).toDTO(any(Payment.class));
  }


  @Test
  @DisplayName("Deve lançar exceção quando serviço de funcionários estiver indisponível")
  void launchPayrollUnavailable() {

    when(feign.findAllActive()).thenReturn(ResponseEntity.status(SERVICE_UNAVAILABLE).build());

    assertThrows(ServicoIndisponivelException.class, () -> service.launchPayroll());

    verify(feign).findAllActive();
    verify(repository, never()).save(any());
  }


  @Test
  @DisplayName("Deve lançar exceção quando resposta do serviço de funcionários estiver vazia")
  void launchPayrollIsNull() {

    when(feign.findAllActive()).thenReturn(ResponseEntity.ok(null));

    assertThrows(ServicoIndisponivelException.class, () -> service.launchPayroll());

    verify(feign).findAllActive();
    verify(repository, never()).save(any());
  }
}


