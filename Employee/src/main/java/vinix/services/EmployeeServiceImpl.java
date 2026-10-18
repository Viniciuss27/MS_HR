package vinix.services;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
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
import vinix.exceptions.ActiveException;
import vinix.exceptions.DuplicateException;
import vinix.exceptions.SaldoInsuficienteException;
import vinix.kafka.events.EmployeeActivatedEvent;
import vinix.kafka.events.EmployeeDeactivatedEvent;
import vinix.kafka.events.EmployeePositionUpdatedEvent;
import vinix.kafka.events.VacationApprovedEvent;
import vinix.kafka.producer.OutboxService;
import vinix.mapper.EmployeeMapper;
import vinix.mapper.VacationMapper;
import vinix.repositories.EmployeeRepository;
import vinix.exceptions.MinimumAgeException;
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
  private final OutboxService outbox;

  @Override
  @Transactional(readOnly = true)
  public List<EmployeeResponseDTO> findAll() {
    return repository.findAll().stream().map(mapper::toDTO).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public EmployeeDetailsResponseDTO findMe() {
    return mapper.toDetailsDTO(verificaId(employeeIdAtual()));
  }

  @Override
  @Transactional(readOnly = true)
  public EmployeeResponseDTO findById(Long id) {
    return mapper.toDTO(verificaId(id));
  }

  @Override
  @Transactional(readOnly = true)
  public EmployeeSalaryResponseDTO findSalaryById(Long id) {
    return mapper.toSalaryDTO(verificaId(id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<EmployeeSalaryResponseDTO> findAllActiveSalary() {
    return repository.findByActive(true).stream().map(mapper::toSalaryDTO).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<EmployeeResponseDTO> findAllActive() {
    return repository.findByActive(true).stream().map(mapper::toDTO).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<EmployeeResponseDTO> findAllInactive() {
    return repository.findByActive(false).stream().map(mapper::toDTO).toList();
  }

  @Override
  @Transactional
  public VacationResponseDTO vacationRequest(VacationRequestDTO dto) {
    Long employeeId = employeeIdAtual();
    Employee employee = verificaId(employeeId);
    validaPeriodo(dto);
    VacationRequest salvo = vacationRepository.save(montarSolicitacao(employee, dto));
    return vacationMapper.toResponseDTO(salvo);
  }

  @Override @Transactional(readOnly = true)
  public List<VacationResponseDTO> findMyVacations() {
    return vacationRepository.findByEmployeeId(employeeIdAtual())
        .stream().map(vacationMapper::toResponseDTO).toList();
  }

  @Override
  @Transactional
  public VacationResponseDTO vacationApprove(Long id) {
    VacationRequest verificado = verificaVacationId(id);
    validaDecisao(verificado);
    verificado.setStatus(VacationStatus.APROVADA);
    verificado.setDecidedAt(Instant.now());
    verificado.setDecidedBy(employeeIdAtual());
    VacationRequest salvo = vacationRepository.save(verificado);

    outbox.saveVacationApproved(new VacationApprovedEvent(salvo.getEmployee().getId(),
        salvo.getEmployee().getName(), salvo.getEmployee().getDailyIncome(), salvo.getId(),
        salvo.getDecidedBy(), salvo.getDecidedAt(), salvo.getStartDate(), salvo.getEndDate(), salvo.getDaysRequested()));
    return vacationMapper.toResponseDTO(salvo);
  }

  @Override
  @Transactional
  public VacationResponseDTO vacationReject(Long id) {
    VacationRequest verificado = verificaVacationId(id);
    validaDecisao(verificado);
    verificado.setStatus(VacationStatus.REPROVADA);
    verificado.setDecidedAt(Instant.now());
    verificado.setDecidedBy(employeeIdAtual());
    VacationRequest salvo = vacationRepository.save(verificado);
    return vacationMapper.toResponseDTO(salvo);
  }

  @Override
  @Transactional
  public VacationResponseDTO vacationRequestForEmployee(Long employeeId, VacationRequestDTO dto) {
      Employee employee = verificaId(employeeId);
      validaPeriodo(dto);
      VacationRequest salvo = vacationRepository.save(montarSolicitacao(employee, dto));
      return vacationMapper.toResponseDTO(salvo);
    }

  @Override
  @Transactional
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

  @Override
  @Transactional
  public EmployeeResponseDTO updatePosition(Long id, EmployeePositionRequestDTO dto) {
    Employee employee = verificaId(id);
    String oldPosition = employee.getPosition();
    mapper.updatePosition(dto, employee);
    Employee salvo = repository.save(employee);

    outbox.saveEmployeePositionUpdated(new EmployeePositionUpdatedEvent(salvo.getId(),
        salvo.getName(), oldPosition, dto.position(), Instant.now()));
    return mapper.toDTO(salvo);
  }

  @Override
  @Transactional
  public EmployeeResponseDTO activate(Long id) {
    Employee employee = verificaId(id);
    if (employee.getActive()) {
      throw new ActiveException("Funcionário já está ativo");
    }

    employee.setActive(true);
    Employee ativo = repository.save(employee);

    outbox.saveEmployeeActivated(new EmployeeActivatedEvent(ativo.getId(), ativo.getName(),Instant.now()));
    return mapper.toDTO(ativo);
  }

  @Override
  @Transactional
  public EmployeeResponseDTO deactivate(Long id) {
    Employee employee = verificaId(id);
    if(!employee.getActive()) {
      throw new ActiveException("Funcionário ja está inativo");
    }

    employee.setActive(false);
    Employee inativo = repository.save(employee);

    outbox.saveEmployeeDeactivated(new EmployeeDeactivatedEvent(inativo.getId(), inativo.getName(),Instant.now()));
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
      throw new DuplicateException("CPF já existente");
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
    LocalDate acquisitionStart = inicioPeriodoConcluido(hireDate);
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

  private LocalDate inicioPeriodoConcluido(LocalDate hireDate) {
    LocalDate hoje = LocalDate.now();
    if (hireDate.plusYears(1).isAfter(hoje)) {
      throw new SaldoInsuficienteException(
          "Funcionário ainda não completou 12 meses de trabalho");
    }
    LocalDate inicio = hireDate;
    while (!inicio.plusYears(2).isAfter(hoje)) {
      inicio = inicio.plusYears(1);
    }
    return inicio; // último período aquisitivo já concluído
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

  private void validaDecisao(VacationRequest vacation) {
    if (vacation.getStatus() != VacationStatus.SOLICITADA) {
      throw new SaldoInsuficienteException(
          "Só é possível decidir solicitações SOLICITADA, Status atual: " + vacation.getStatus());
    }
    if (vacation.getEmployee().getId().equals(employeeIdAtual())) {
      throw new AccessDeniedException("Não é possível decidir a própria solicitação de férias");
    }
  }

  private void validaPeriodo(VacationRequestDTO dto) {
    if (dto.endDate().isBefore(dto.startDate())) {
      throw new SaldoInsuficienteException("A data final não pode ser anterior à data inicial");
    }
    if (dto.startDate().isBefore(LocalDate.now())) {
      throw new SaldoInsuficienteException("A data inicial não pode estar no passado");
    }
  }

}

