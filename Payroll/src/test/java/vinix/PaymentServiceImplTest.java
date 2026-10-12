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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import vinix.dto.request.PaymentRequestDTO;
import vinix.dto.response.PaymentResponseDTO;
import vinix.entities.Payment;
import vinix.entities.PaymentStatus;
import vinix.entities.PaymentType;
import vinix.exceptions.ResourceNotFoundException;
import vinix.exceptions.ServicoIndisponivelException;
import vinix.feign.EmployeeDTO;
import vinix.feign.EmployeeFeignClient;
import vinix.kafka.events.producer.PaymentCanceledEvent;
import vinix.kafka.events.producer.PaymentCreatedEvent;
import vinix.kafka.events.producer.PaymentRefundRequestedEvent;
import vinix.kafka.producer.ProducerService;
import vinix.mapper.PaymentMapper;
import vinix.repositories.PaymentRepository;
import vinix.services.PaymentServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentServiceImplTest")
class PaymentServiceImplTest {

  private static final String TOKEN = "Bearer token";
  private static final LocalDate HIRE_DATE = LocalDate.of(2020, 1, 10);

  @Mock private EmployeeFeignClient feign;
  @Mock private PaymentMapper mapper;
  @Mock private PaymentRepository repository;
  @Mock private ProducerService kafka;

  @InjectMocks
  private PaymentServiceImpl service;

  @BeforeEach
  void setUp() {
    Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").claim("employeeId", 1L).build();
    SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
  }

  @AfterEach
  void tearDown() { SecurityContextHolder.clearContext(); }

  private PaymentResponseDTO dto(Long id, Long employeeId, String name, BigDecimal amount,
                                 PaymentStatus status, PaymentType type) {
    return new PaymentResponseDTO(
        id, employeeId, name, amount, status, null, type, LocalDate.of(2026, 9, 1));
  }

     // CONSULTAS

  @Test
  @DisplayName("Deve retornar todos os pagamentos")
  void findAll() {
    Payment payment = Payment.builder().id(1L).employeeId(10L).employeeName("João").build();
    PaymentResponseDTO dto = dto(
        1L, 10L, "João", new BigDecimal("3000.00"), PaymentStatus.PENDING, PaymentType.SALARY);

    when(repository.findAll()).thenReturn(List.of(payment));
    when(mapper.toDTO(payment)).thenReturn(dto);

    List<PaymentResponseDTO> result = service.findAll();

    assertEquals(1, result.size());
    assertEquals(dto, result.get(0));
  }

  @Test
  @DisplayName("Deve encontrar pagamento pelo ID")
  void findById() {
    Payment payment = Payment.builder().id(1L).employeeId(10L).employeeName("João").build();
    PaymentResponseDTO dto = dto(
        1L, 10L, "João", BigDecimal.ZERO, PaymentStatus.PENDING, PaymentType.SALARY);

    when(repository.findById(1L)).thenReturn(Optional.of(payment));
    when(mapper.toDTO(payment)).thenReturn(dto);

    assertEquals(dto, service.findById(1L));
  }

  @Test
  @DisplayName("Deve lançar exceção quando pagamento não existir")
  void findByIdNotFound() {
    when(repository.findById(1L)).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> service.findById(1L));
    verify(mapper, never()).toDTO(any());
  }

  @Test
  @DisplayName("findMyPayments usa o employeeId do token e não chama o Employee")
  void findMyPayments() {
    Payment payment = Payment.builder().id(1L).employeeId(1L).employeeName("João").build();
    PaymentResponseDTO dto = dto(
        1L, 1L, "João", BigDecimal.ZERO, PaymentStatus.PENDING, PaymentType.SALARY);

    when(repository.findByEmployeeId(1L)).thenReturn(List.of(payment));
    when(mapper.toDTO(payment)).thenReturn(dto);

    List<PaymentResponseDTO> result = service.findMyPayments();

    assertEquals(1, result.size());
    verify(repository).findByEmployeeId(1L);
    verifyNoInteractions(feign);
  }

  @Test
  @DisplayName("Deve retornar pagamentos do funcionário")
  void findByEmployeeId() {
    EmployeeDTO worker = new EmployeeDTO(10L, "João", new BigDecimal("100.00"), HIRE_DATE);
    Payment payment = Payment.builder().id(1L).employeeId(10L).employeeName("João").build();
    PaymentResponseDTO dto = dto(
        1L, 10L, "João", BigDecimal.ZERO, PaymentStatus.PENDING, PaymentType.SALARY);

    when(feign.findById(10L, TOKEN)).thenReturn(ResponseEntity.ok(worker));
    when(repository.findByEmployeeId(10L)).thenReturn(List.of(payment));
    when(mapper.toDTO(payment)).thenReturn(dto);

    List<PaymentResponseDTO> result = service.findByEmployeeId(10L);

    assertEquals(1, result.size());
    assertEquals(dto, result.get(0));
    verify(feign).findById(10L, TOKEN);
  }

  @Test
  @DisplayName("Deve lançar exceção quando funcionário não existir")
  void findByEmployeeIdNotFound() {
    when(feign.findById(10L, TOKEN)).thenReturn(ResponseEntity.notFound().build());
    assertThrows(ResourceNotFoundException.class, () -> service.findByEmployeeId(10L));
    verify(repository, never()).findByEmployeeId(anyLong());
  }

  @Test
  @DisplayName("Deve lançar exceção quando serviço de funcionário estiver indisponível")
  void findByEmployeeIdServiceUnavailable() {
    when(feign.findById(10L, TOKEN)).thenReturn(ResponseEntity.status(SERVICE_UNAVAILABLE).build());
    assertThrows(ServicoIndisponivelException.class, () -> service.findByEmployeeId(10L));
    verify(repository, never()).findByEmployeeId(anyLong());
  }

  @Test
  @DisplayName("Deve listar apenas pagamentos de férias")
  void vacationRequestAll() {
    Payment payment = Payment.builder().id(1L).type(PaymentType.VACATION).build();
    PaymentResponseDTO dto = dto(
        1L, 10L, "João", BigDecimal.ZERO, PaymentStatus.PENDING, PaymentType.VACATION);

    when(repository.findByType(PaymentType.VACATION)).thenReturn(List.of(payment));
    when(mapper.toDTO(payment)).thenReturn(dto);

    assertEquals(1, service.vacationRequestAll().size());
  }

  @Test
  @DisplayName("Deve buscar o pagamento de férias pelo id da solicitação")
  void vacationRequestId() {
    Payment payment = Payment.builder().id(1L).vacationRequestId(42L).type(PaymentType.VACATION).build();
    PaymentResponseDTO dto = dto(
        1L, 10L, "João", BigDecimal.ZERO, PaymentStatus.PENDING, PaymentType.VACATION);

    when(repository.findByVacationRequestId(42L)).thenReturn(Optional.of(payment));
    when(mapper.toDTO(payment)).thenReturn(dto);

    assertEquals(dto, service.vacationRequestId(42L));
  }

  @Test
  @DisplayName("Deve lançar exceção quando não houver pagamento para a solicitação de férias")
  void vacationRequestIdNotFound() {
    when(repository.findByVacationRequestId(42L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> service.vacationRequestId(42L));
  }

       //CREATE

  @Test
  @DisplayName("Deve criar um pagamento salarial")
  void create() {
    PaymentRequestDTO request = new PaymentRequestDTO(
        10L, 30, LocalDate.of(2026, 9, 1));
    EmployeeDTO worker = new EmployeeDTO(10L, "João", new BigDecimal("100.00"), HIRE_DATE);
    PaymentResponseDTO response = dto(
        1L, 10L, "João", new BigDecimal("3000.00"), PaymentStatus.PENDING, PaymentType.SALARY);

    when(feign.findById(10L, TOKEN)).thenReturn(ResponseEntity.ok(worker));
    when(repository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));
    when(mapper.toDTO(any(Payment.class))).thenReturn(response);

    PaymentResponseDTO result = service.create(request);

    ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
    verify(repository).save(captor.capture());
    Payment saved = captor.getValue();

    assertEquals(response, result);
    assertEquals(new BigDecimal("3000.00"), saved.getGrossAmount());
    assertEquals(PaymentType.SALARY, saved.getType());
    assertEquals(PaymentStatus.PENDING, saved.getStatus());
    verify(kafka).publishCreatedEvent(any(PaymentCreatedEvent.class));
  }

  @Test
  @DisplayName("Deve calcular e criar pagamento de férias")
  void calculateVacation() {
    PaymentRequestDTO request = new PaymentRequestDTO(
        10L, 30, LocalDate.of(2026, 10, 1));
    EmployeeDTO worker = new EmployeeDTO(10L, "João", new BigDecimal("100.00"), HIRE_DATE);
    PaymentResponseDTO response = dto(
        3L, 10L, "João", new BigDecimal("3000.00"), PaymentStatus.PENDING, PaymentType.VACATION);

    when(feign.findById(10L, TOKEN)).thenReturn(ResponseEntity.ok(worker));
    when(repository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));
    when(mapper.toDTO(any(Payment.class))).thenReturn(response);

    assertEquals(response, service.calculateVacation(request));

    verify(kafka).publishCreatedEvent(any(PaymentCreatedEvent.class));
  }

       // 13º salário

  private Payment calcula13(LocalDate hireDate) {
    PaymentRequestDTO request = new PaymentRequestDTO(
        10L, 30, LocalDate.of(2026, 12, 31));
    EmployeeDTO worker = new EmployeeDTO(10L, "João", new BigDecimal("100.00"), hireDate);

    when(feign.findById(10L, TOKEN)).thenReturn(ResponseEntity.ok(worker));
    when(repository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));
    when(mapper.toDTO(any(Payment.class))).thenReturn(
        dto(2L, 10L, "João", BigDecimal.ZERO, PaymentStatus.PENDING, PaymentType.THIRTEENTH));

    service.calculate13Salary(request);

    ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
    verify(repository).save(captor.capture());
    return captor.getValue();
  }

  @Test
  @DisplayName("13º: ano completo paga 12/12")
  void calculate13SalaryFullYear() {
    Payment saved = calcula13(HIRE_DATE);

    assertEquals(12, saved.getDaysWorked());
    assertEquals(new BigDecimal("3000.00"), saved.getGrossAmount());
    assertEquals(PaymentType.THIRTEENTH, saved.getType());
  }

  @Test
  @DisplayName("13º: admitido em 10/07 trabalhou 22 dias em julho, então conta o mês (6/12)")
  void calculate13SalaryHiredMidJuly() {
    Payment saved = calcula13(LocalDate.of(2026, 7, 10));

    assertEquals(6, saved.getDaysWorked());
    assertEquals(new BigDecimal("1500.00"), saved.getGrossAmount());
  }

  @Test
  @DisplayName("13º: admitido em 20/07 trabalhou só 12 dias em julho, então o mês não conta (5/12)")
  void calculate13SalaryHiredLateJuly() {
    Payment saved = calcula13(LocalDate.of(2026, 7, 20));

    assertEquals(5, saved.getDaysWorked());
    assertEquals(new BigDecimal("1250.00"), saved.getGrossAmount());
  }

     // status e cancelamento

  @Test
  @DisplayName("Deve atualizar o status do pagamento")
  void updateStatus() {
    Payment payment = Payment.builder().id(1L).status(PaymentStatus.PENDING).build();
    PaymentResponseDTO response = dto(
        1L, 10L, "João", BigDecimal.ZERO, PaymentStatus.PAID, PaymentType.SALARY);

    when(repository.findById(1L)).thenReturn(Optional.of(payment));
    when(repository.save(payment)).thenReturn(payment);
    when(mapper.toDTO(payment)).thenReturn(response);

    PaymentResponseDTO result = service.updateStatus(1L, PaymentStatus.PAID);

    assertEquals(PaymentStatus.PAID, payment.getStatus());
    assertEquals(response, result);
  }

  @Test
  @DisplayName("Não deve permitir alterar status diretamente para CANCELED")
  void updateStatusCanceled() {
    assertThrows(IllegalArgumentException.class, () -> service.updateStatus(1L, PaymentStatus.CANCELED));

    verify(repository, never()).findById(anyLong());
    verify(repository, never()).save(any());
  }

  @Test
  @DisplayName("Deve cancelar pagamento PENDING")
  void cancel() {
    Payment payment = Payment.builder().id(1L).status(PaymentStatus.PENDING).build();
    PaymentResponseDTO response = dto(
        1L, 10L, "João", BigDecimal.ZERO, PaymentStatus.CANCELED, PaymentType.SALARY);

    when(repository.findById(1L)).thenReturn(Optional.of(payment));
    when(repository.save(payment)).thenReturn(payment);
    when(mapper.toDTO(payment)).thenReturn(response);

    PaymentResponseDTO result = service.cancel(1L);

    assertEquals(PaymentStatus.CANCELED, payment.getStatus());
    assertEquals(response, result);
    verify(kafka).publishCanceledEvent(any(PaymentCanceledEvent.class));
    verify(kafka, never()).publishRefundRequestedEvent(any());
  }

  @Test
  @DisplayName("Deve cancelar pagamento PAID e pedir estorno")
  void cancelPaid() {
    Payment payment = Payment.builder().id(1L).status(PaymentStatus.PAID).build();
    PaymentResponseDTO response = dto(
        1L, 10L, "João", BigDecimal.ZERO, PaymentStatus.CANCELED, PaymentType.SALARY);

    when(repository.findById(1L)).thenReturn(Optional.of(payment));
    when(repository.save(payment)).thenReturn(payment);
    when(mapper.toDTO(payment)).thenReturn(response);

    PaymentResponseDTO result = service.cancel(1L);

    assertEquals(PaymentStatus.CANCELED, payment.getStatus());
    assertEquals(response, result);
    verify(kafka).publishRefundRequestedEvent(any(PaymentRefundRequestedEvent.class));
    verify(kafka, never()).publishCanceledEvent(any());
  }

  @Test
  @DisplayName("Não deve cancelar pagamento com status inválido")
  void cancelInvalidStatus() {
    Payment payment = Payment.builder().id(1L).status(PaymentStatus.FAILED).build();
    when(repository.findById(1L)).thenReturn(Optional.of(payment));
    assertThrows(IllegalStateException.class, () -> service.cancel(1L));
    verify(repository, never()).save(any());
  }

  @Test
  @DisplayName("Cancelar pagamento já cancelado não salva nem publica nada")
  void cancelAlreadyCanceled() {
    Payment payment = Payment.builder().id(1L).status(PaymentStatus.CANCELED).build();
    PaymentResponseDTO response = dto(
        1L, 10L, "João", BigDecimal.ZERO, PaymentStatus.CANCELED, PaymentType.SALARY);

    when(repository.findById(1L)).thenReturn(Optional.of(payment));
    when(mapper.toDTO(payment)).thenReturn(response);

    assertEquals(response, service.cancel(1L));

    verify(repository, never()).save(any());
    verifyNoInteractions(kafka);
  }

  @Test
  @DisplayName("Deve lançar exceção ao cancelar pagamento inexistente")
  void cancelNotFound() {
    when(repository.findById(1L)).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> service.cancel(1L));
    verify(repository, never()).save(any());
  }

         // folha em lote

  @Test
  @DisplayName("Deve lançar a folha de pagamento para todos os funcionários")
  void launchPayroll() {
    EmployeeDTO worker1 = new EmployeeDTO(1L, "João", new BigDecimal("100.00"), HIRE_DATE);
    EmployeeDTO worker2 = new EmployeeDTO(2L, "Maria", new BigDecimal("150.00"), HIRE_DATE);

    PaymentResponseDTO dto1 = dto(
        1L, 1L, "João", new BigDecimal("3000.00"), PaymentStatus.PENDING, PaymentType.SALARY);
    PaymentResponseDTO dto2 = dto(
        2L, 2L, "Maria", new BigDecimal("4500.00"), PaymentStatus.PENDING, PaymentType.SALARY);

    when(feign.findAllActive(TOKEN)).thenReturn(ResponseEntity.ok(List.of(worker1, worker2)));
    when(repository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));
    when(mapper.toDTO(any(Payment.class))).thenReturn(dto1, dto2);

    List<PaymentResponseDTO> result = service.launchPayroll();

    assertEquals(2, result.size());
    assertEquals(dto1, result.get(0));
    assertEquals(dto2, result.get(1));
    verify(repository, times(2)).save(any(Payment.class));
    verify(kafka, times(2)).publishCreatedEvent(any(PaymentCreatedEvent.class));
  }

  @Test
  @DisplayName("Deve lançar exceção quando serviço de funcionários estiver indisponível")
  void launchPayrollUnavailable() {
    when(feign.findAllActive(TOKEN)).thenReturn(ResponseEntity.status(SERVICE_UNAVAILABLE).build());
    assertThrows(ServicoIndisponivelException.class, () -> service.launchPayroll());
    verify(repository, never()).save(any());
  }

  @Test
  @DisplayName("Deve lançar exceção quando resposta do serviço de funcionários estiver vazia")
  void launchPayrollIsNull() {
    when(feign.findAllActive(TOKEN)).thenReturn(ResponseEntity.ok(null));
    assertThrows(ServicoIndisponivelException.class, () -> service.launchPayroll());
    verify(repository, never()).save(any());
  }
}