package vinix.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vinix.dto.request.EmployeePositionRequestDTO;
import vinix.dto.request.EmployeeRequestDTO;
import vinix.dto.request.VacationRequestDTO;
import vinix.dto.response.EmployeeDetailsResponseDTO;
import vinix.dto.response.EmployeeResponseDTO;
import vinix.dto.response.EmployeeSalaryResponseDTO;
import vinix.dto.response.VacationResponseDTO;
import vinix.entities.Employee;
import vinix.entities.VacationRequest;
import vinix.entities.VacationStatus;
import vinix.exceptions.SaldoInsuficienteException;
import vinix.kafka.events.EmployeeActivatedEvent;
import vinix.kafka.events.EmployeeDeactivatedEvent;
import vinix.kafka.events.EmployeePositionUpdatedEvent;
import vinix.kafka.producer.ProducerService;
import vinix.mapper.EmployeeMapper;
import vinix.mapper.VacationMapper;
import vinix.repositories.EmployeeRepository;
import vinix.exceptions.ActiveException;
import vinix.exceptions.MinimumAgeException;
import vinix.exceptions.DuplicateCpfException;
import vinix.exceptions.ResourceNotFoundException;
import vinix.repositories.VacationRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

  private final EmployeeRepository repository;
  private final VacationRepository vacationRepository;
  private final EmployeeMapper mapper;
  private final VacationMapper  vacationMapper;
  private final ProducerService kafka;

  @Override @Transactional(readOnly = true)
  public List<EmployeeResponseDTO> findAll() {
    return repository.findAll().stream().map(mapper::toDTO).toList();
  }

  @Override @Transactional(readOnly = true)
  public EmployeeSalaryResponseDTO findSalaryById(Long id) {
    return mapper.toSalaryDTO(verificaId(id));
  }

  @Override @Transactional(readOnly = true)
  public EmployeeResponseDTO findById(Long id) {return mapper.toDTO(verificaId(id));}

  @Override @Transactional(readOnly = true)
  public EmployeeDetailsResponseDTO findByCpf(String cpf) {
    return mapper.toDetailsDTO(repository.findByCpf(cpf).orElseThrow(() ->
        new ResourceNotFoundException("CPF não encontrado: " + cpf)));
  }

  @Override @Transactional(readOnly = true)
  @PreAuthorize("hasRole('HR')")
  public List<EmployeeResponseDTO> findAllActive() {
    return repository.findByActive(true).stream().map(mapper::toDTO).toList();
  }

  @Override @Transactional(readOnly = true)
  @PreAuthorize("hasAnyRole('HR')")
  public List<EmployeeResponseDTO> findAllInactive() {
    return repository.findByActive(false).stream().map(mapper::toDTO).toList();
  }

  @Override @Transactional
  @PreAuthorize("isAuthenticated()")
  public VacationResponseDTO vacationRequest(VacationRequestDTO dto) {
    Long employeeId = employeeIdAtual();
    Employee employee = verificaId(employeeId);
    VacationRequest salvo = vacationRepository.save(montarSolicitacao(employee, dto));
    return vacationMapper.toResponseDTO(salvo);
  }

  @Override @Transactional
  @PreAuthorize("hasAnyRole('HR', 'MANAGER')")
  public VacationResponseDTO vacationApprove(Long id) {
    VacationRequest verificado = verificaVacationId(id);
    verificado.setStatus(VacationStatus.APROVADA);
    verificado.setDecidedAt(Instant.now());
    verificado.setDecidedBy(employeeIdAtual());
    VacationRequest salvo = vacationRepository.save(verificado);
    return vacationMapper.toResponseDTO(salvo);
  }

  @Override @Transactional
  @PreAuthorize("hasAnyRole('HR', 'MANAGER')")
  public VacationResponseDTO vacationReject(Long id) {
    VacationRequest verificado = verificaVacationId(id);
    verificado.setStatus(VacationStatus.REPROVADA);
    verificado.setDecidedAt(Instant.now());
    verificado.setDecidedBy(employeeIdAtual());
    VacationRequest salvo = vacationRepository.save(verificado);
    return vacationMapper.toResponseDTO(salvo);
  }

  @Override @Transactional
  @PreAuthorize("hasRole('HR')")
  public VacationResponseDTO vacationRequestForEmployee(Long employeeId, VacationRequestDTO dto) {
      Employee employee = verificaId(employeeId);
      VacationRequest salvo = vacationRepository.save(montarSolicitacao(employee, dto));
      return vacationMapper.toResponseDTO(salvo);
    }

  @Override @Transactional
  @PreAuthorize("hasRole('HR')")
  public EmployeeResponseDTO create(EmployeeRequestDTO dto) {
    String cpfLimpo = limparCpf(dto.cpf());
    validaIdade(dto.birthDate());
    validaCPF(cpfLimpo);
    Employee employee = mapper.toEntity(dto);
    employee.setCpf(cpfLimpo);
    employee.setActive(true);
    employee.setHireDate(LocalDate.now());

    Employee salvo = repository.save(employee);
    return mapper.toDTO(salvo);
  }

  @Override @Transactional
  @PreAuthorize("hasRole('HR')")
  public EmployeeResponseDTO updatePosition(Long id, EmployeePositionRequestDTO dto) {
    Employee employee = verificaId(id);
    String oldPosition = employee.getPosition();
    mapper.updatePosition(dto, employee);
    Employee salvo = repository.save(employee);

    EmployeePositionUpdatedEvent event = new EmployeePositionUpdatedEvent(salvo.getId(), salvo.getName(),
        oldPosition, dto.position(), Instant.now());
    kafka.publishEmployeePositionUpdated(event);

    return mapper.toDTO(salvo);
  }

  @Override @Transactional
  @PreAuthorize("hasRole('HR')")
  public EmployeeResponseDTO activate(Long id) {
    Employee employee = verificaId(id);
    if (employee.getActive()) {
      throw new ActiveException("Funcionário já está ativo");
    }

    employee.setActive(true);
    Employee ativo = repository.save(employee);

    EmployeeActivatedEvent event = new EmployeeActivatedEvent(ativo.getId(), ativo.getName(),Instant.now());
    kafka.publishEmployeeActivated(event);

    return mapper.toDTO(ativo);
  }

  @Override @Transactional
  @PreAuthorize("hasRole('HR')")
  public EmployeeResponseDTO deactivate(Long id) {
    Employee employee = verificaId(id);
    if(!employee.getActive()) {
      throw new ActiveException("Funcionário ja está inativo");
    }

    employee.setActive(false);
    Employee inativo = repository.save(employee);

    EmployeeDeactivatedEvent event = new EmployeeDeactivatedEvent(inativo.getId(), inativo.getName(),Instant.now());
    kafka.publishEmployeeDeactivated(event);

    return mapper.toDTO(inativo);
  }

  private Employee verificaId(Long id){
    return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Id não encontrado: " + id));
  }

  private VacationRequest verificaVacationId(Long id){
    return vacationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Id não encontrado: " + id));
  }

  private void validaIdade(LocalDate birthDate) {
    int idade = Period.between(birthDate, LocalDate.now()).getYears();
    if (idade < 18) {
      throw new MinimumAgeException("O funcionário deve ter no mínimo 18 anos, Idade informada: " + idade);
    }
  }

  private void validaCPF(String cpf) {
    if(repository.existsByCpf(cpf)){
      throw new DuplicateCpfException("CPF já existente");
    }
  }

  private String limparCpf(String cpf) {
    return cpf.replaceAll("[^0-9]", "");
  }

  private Long employeeIdAtual() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
      Object claim = jwt.getClaim("employeeId");
      if (claim instanceof Number number) {
        return number.longValue();
      }
    }
    throw new ResourceNotFoundException("Nenhum usuário autenticado no contexto atual");
  }

  private VacationRequest montarSolicitacao(Employee employee, VacationRequestDTO dto) {
    long dias = ChronoUnit.DAYS.between(dto.startDate(), dto.endDate()) + 1;

    LocalDate hireDate = employee.getHireDate();
    LocalDate acquisitionStart = calcularInicioPeriodoAquisitivo(hireDate);
    LocalDate acquisitionEnd = acquisitionStart.plusYears(1).minusDays(1);
    validaSaldo(employee, acquisitionStart, (int) dias);

    VacationRequest vacation = vacationMapper.toEntity(dto);
    vacation.setEmployee(employee);
    vacation.setStatus(VacationStatus.SOLICITADA);
    vacation.setDaysRequested((int) dias);
    vacation.setAcquisitionStartDate(acquisitionStart);
    vacation.setAcquisitionEndDate(acquisitionEnd);
    vacation.setUnjustifiedAbsences(0);
    return vacation;
  }

  private LocalDate calcularInicioPeriodoAquisitivo(LocalDate hireDate) {
    LocalDate hoje = LocalDate.now();
    LocalDate inicio = hireDate;
    while (!inicio.plusYears(1).isAfter(hoje)) {
      inicio = inicio.plusYears(1);
    }
    return inicio;
  }

  private void validaSaldo(Employee employee, LocalDate acquisitionStart, int diasSolicitados) {
    int diasJaGastos = vacationRepository.findByEmployeeAndAcquisitionStartDateAndStatusIn(
            employee, acquisitionStart, List.of(VacationStatus.APROVADA,
                VacationStatus.PROGRAMADA, VacationStatus.INICIADA, VacationStatus.FINALIZADA))
        .stream().mapToInt(VacationRequest::getDaysRequested).sum();

    int saldoDisponivel = 30 - diasJaGastos; // 30 dias é o padrão CLT por período aquisitivo

    if (diasSolicitados > saldoDisponivel) {
      throw new SaldoInsuficienteException(
          "Saldo insuficiente: disponível " + saldoDisponivel + " dias, solicitado " + diasSolicitados);
    }
  }
}

