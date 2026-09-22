package vinix.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import vinix.services.exceptions.ResourceNotFoundException;
import vinix.services.exceptions.ServicoIndisponivelException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

  private final EmployeeFeignClient feign;
  private final PaymentMapper mapper;
  private final PaymentRepository repository;
  private final ProducerService kafka;

  @Override @Transactional(readOnly = true)
  public List<PaymentResponseDTO> findAll() {
    return repository.findAll().stream().map(mapper::toDTO).toList();
  }

  @Override @Transactional
  @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
  public List<PaymentResponseDTO> launchPayroll() {
    ResponseEntity<List<EmployeeDTO>> response = feign.findAllActive(tokenAtual());

    if (response.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE || response.getBody() == null) {
      throw new ServicoIndisponivelException(
          "O serviço de funcionários está indisponível no momento, tente novamente mais tarde");
    }

    List<EmployeeDTO> funcionarios = response.getBody();
    List<PaymentResponseDTO> pagamentos = new ArrayList<>();

    int dias = 30;
    LocalDate referenceDate = LocalDate.now();

    for (EmployeeDTO funcionario : funcionarios) {
      Payment payment = montarPagamento(funcionario, dias, referenceDate, PaymentType.SALARY);
      payment = repository.save(payment);

      publicarCriacao(payment);
      pagamentos.add(mapper.toDTO(payment));
    }

    return pagamentos;
  }

  @Override @Transactional(readOnly = true)
  public PaymentResponseDTO findById(Long id) {
    return mapper.toDTO(buscarPagamento(id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<PaymentResponseDTO> findByEmployeeId(Long employeeId) {
    validaEmployeeId(employeeId);
    return repository.findByEmployeeId(employeeId).stream().map(mapper::toDTO).toList();
  }

  @Override @Transactional
  @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
  public PaymentResponseDTO create(PaymentRequestDTO dto) {
    Payment payment = montarPagamento(dto.employeeId(), dto.daysWorked(), dto.referenceDate(), PaymentType.SALARY);
    payment = repository.save(payment);

    publicarCriacao(payment);
    return mapper.toDTO(payment);
  }

  @Override @Transactional
  @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
  public PaymentResponseDTO calculate13Salary(PaymentRequestDTO dto) {
    Payment payment = montarPagamento(dto.employeeId(), dto.daysWorked(), dto.referenceDate(), PaymentType.THIRTEENTH);
    payment = repository.save(payment);

    publicarCriacao(payment);
    return mapper.toDTO(payment);
  }

  @Override @Transactional
  @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
  public PaymentResponseDTO calculateVacation(PaymentRequestDTO dto) {
    Payment payment = montarPagamento(dto.employeeId(), dto.daysWorked(), dto.referenceDate(), PaymentType.VACATION);
    payment = repository.save(payment);

    publicarCriacao(payment);
    return mapper.toDTO(payment);
  }

  @Override @Transactional
  @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
  public PaymentResponseDTO updateStatus(Long id, PaymentStatus status) {
    if (status == PaymentStatus.CANCELED) {
      throw new IllegalArgumentException(
          "Use o método cancel() para cancelar um pagamento — ele aplica a regra de estorno!");
    }

    Payment payment = buscarPagamento(id);
    payment.setStatus(status);
    payment = repository.save(payment);

    // kafka consumer para receber confirmação do financeiro e atualizar status para PAID
    // este método é a alteração manual de status; a confirmação automática virá por evento
    return mapper.toDTO(payment);
  }

  @Override @Transactional
  @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
  public PaymentResponseDTO cancel(Long id) {
    Payment payment = buscarPagamento(id);

    switch (payment.getStatus()) {
      case PAID -> {
        payment.setStatus(PaymentStatus.CANCELED);
        payment = repository.save(payment);

        log.info("Pagamento ID: {} cancelado. Estorno será solicitado ao serviço financeiro!", id);
        kafka.publishRefundRequestedEvent(new PaymentRefundRequestedEvent(payment.getId(), payment.getEmployeeId(),
            payment.getGrossAmount(), Instant.now()));
      }
      case PENDING -> {
        payment.setStatus(PaymentStatus.CANCELED);
        payment = repository.save(payment);

        log.info("Pagamento ID: {} cancelado", id);
        kafka.publishCanceledEvent(new PaymentCanceledEvent(payment.getId()
            , payment.getEmployeeId(), Instant.now()));
      }
      case CANCELED -> {
        log.info("Pagamento ID: {} já está cancelado!", id);
      }
      default -> {
        throw new IllegalStateException("Não é possível cancelar o pagamento ID: " + id + " no status " + payment.getStatus());
      }
    }

    return mapper.toDTO(payment);
  }

  private void publicarCriacao(Payment payment) {
    kafka.publishCreatedEvent(new PaymentCreatedEvent(
        payment.getId(), payment.getEmployeeId(), payment.getEmployeeName(),
        payment.getGrossAmount(), payment.getType(), payment.getReferenceDate(), Instant.now()));
  }

  private String tokenAtual() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
      return "Bearer " + jwt.getTokenValue();
    }
    throw new IllegalStateException("Nenhum usuário autenticado no contexto atual");
  }

  private EmployeeDTO validaEmployeeId(Long employeeId) {
    ResponseEntity<EmployeeDTO> response = feign.findById(employeeId, tokenAtual());

    if (response.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
      throw new ServicoIndisponivelException("O serviço está indisponível no momento. Tente novamente mais tarde");
    }

    if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
      throw new ResourceNotFoundException(employeeId + " -> EmployeeId não encontrado");
    }
    return response.getBody();
  }

  // versão que busca o funcionário por ID (create, calculate13Salary, calculateVacation)
  private Payment montarPagamento(Long employeeId, Integer dias, LocalDate referenceDate, PaymentType type) {
    EmployeeDTO employeer = validaEmployeeId(employeeId);
    return montarPagamento(employeer, dias, referenceDate, type);
  }

  // versão que já recebe o funcionário pronto, sem chamar o Feign de novo (launchPayroll, dentro do loop)
  private Payment montarPagamento(EmployeeDTO employee, Integer dias, LocalDate referenceDate, PaymentType type) {
    BigDecimal total = employee.dailyIncome().multiply(BigDecimal.valueOf(dias));

    return Payment.builder()
        .employeeId(employee.id())
        .employeeName(employee.name())
        .dailyIncome(employee.dailyIncome())
        .daysWorked(dias)
        .grossAmount(total)
        .referenceDate(referenceDate)
        .status(PaymentStatus.PENDING)
        .type(type)
        .build();
  }

  private Payment buscarPagamento(Long id) {
    Payment payment = repository.findById(id).orElseThrow(() ->
        new ResourceNotFoundException(id + " -> Id não encontrado"));
    return payment;
  }
}