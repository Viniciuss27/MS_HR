package vinix.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vinix.dto.request.EmployeePositionRequestDTO;
import vinix.dto.request.EmployeeRequestDTO;
import vinix.dto.response.EmployeeDetailsResponseDTO;
import vinix.dto.response.EmployeeResponseDTO;
import vinix.entities.Employee;
import vinix.mapper.EmployeeMapper;
import vinix.repositories.EmployeeRepository;
import vinix.services.exceptions.ActiveException;
import vinix.services.exceptions.MinimumAgeException;
import vinix.services.exceptions.DuplicateCpfException;
import vinix.services.exceptions.ResourceNotFoundException;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@RequiredArgsConstructor
@Service
public class EmployeeServiceImpl implements EmployeeService {

  private final EmployeeRepository repository;
  private final EmployeeMapper mapper;

  @Override @Transactional(readOnly = true)
  public List<EmployeeResponseDTO> findAll() {
    return repository.findAll().stream().map(mapper::toDTO).toList();
  }

  @Override @Transactional(readOnly = true)
  public EmployeeResponseDTO findById(Long id) {return mapper.toDTO(verificaId(id));}

  @Override @Transactional(readOnly = true)
  public EmployeeDetailsResponseDTO findByCpf(String cpf) {
    return mapper.toDetailsDTO(repository.findByCpf(cpf).orElseThrow(() ->
        new ResourceNotFoundException("CPF não encontrado: " + cpf)));
  }

  @Override @Transactional(readOnly = true)
  //@PreAuthorize("hasRole('HR')")
  public List<EmployeeResponseDTO> findAllActive() {
    return repository.findByActive(true).stream().map(mapper::toDTO).toList();
  }

  @Override @Transactional(readOnly = true)
  //@PreAuthorize("hasAnyRole('HR')")
  public List<EmployeeResponseDTO> findAllInactive() {
    return repository.findByActive(false).stream().map(mapper::toDTO).toList();
  }

  @Override @Transactional
  //@PreAuthorize("hasRole('HR')")
  public EmployeeResponseDTO create(EmployeeRequestDTO dto) {
    validaIdade(dto.birthDate());
    validaCPF(dto.cpf());
    Employee employee = mapper.toEntity(dto);
    employee.setActive(true);
    employee.setHireDate(LocalDate.now());

    Employee salvo = repository.save(employee);
    return mapper.toDTO(salvo);
  }

  @Override @Transactional
  //@PreAuthorize("hasRole('HR')")
  public EmployeeResponseDTO updatePosition(Long id, EmployeePositionRequestDTO dto) {
    Employee p = verificaId(id);
    mapper.updatePosition(dto, p);
    Employee salvo = repository.save(p);
    return mapper.toDTO(salvo);

    // kafka -> funcionario promovido( Auth -> novo role)
  }

  @Override @Transactional
  //@PreAuthorize("hasRole('HR')")
  public EmployeeResponseDTO activate(Long id) {
    Employee employee = verificaId(id);
    if (employee.getActive()) {
      throw new ActiveException("Funcionário já está ativo");
    }

    employee.setActive(true);
    Employee ativo = repository.save(employee);
    return mapper.toDTO(ativo);

    // kafka -> funcionario reativado
  }

  @Override @Transactional
  //@PreAuthorize("hasRole('HR')")
  public EmployeeResponseDTO deactivate(Long id) {
    Employee employee = verificaId(id);
    if(!employee.getActive()) {
      throw new ActiveException("Funcionário ja está inativo");
    }

    employee.setActive(false);
    Employee inativo = repository.save(employee);
    return mapper.toDTO(inativo);

    // kafka -> funcionario desativado
  }

  private Employee verificaId(Long id){
    return repository.findById(id).orElseThrow(() ->
        new ResourceNotFoundException("Id não encontrado: " + id));
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

}
