package vinix;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vinix.dto.request.EmployeePositionRequestDTO;
import vinix.dto.request.EmployeeRequestDTO;
import vinix.dto.response.EmployeeDetailsResponseDTO;
import vinix.dto.response.EmployeeResponseDTO;
import vinix.entities.Employee;
import vinix.kafka.events.EmployeeActivatedEvent;
import vinix.kafka.events.EmployeeDeactivatedEvent;
import vinix.kafka.events.EmployeePositionUpdatedEvent;
import vinix.kafka.producer.ProducerService;
import vinix.mapper.EmployeeMapper;
import vinix.repositories.EmployeeRepository;
import vinix.services.EmployeeServiceImpl;
import vinix.services.exceptions.ActiveException;
import vinix.services.exceptions.DuplicateCpfException;
import vinix.services.exceptions.MinimumAgeException;
import vinix.services.exceptions.ResourceNotFoundException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeServiceImplTeste")
class EmployeeServiceImplTest {

	@Mock private EmployeeRepository repository;
	@Mock private EmployeeMapper mapper;
	@Mock private ProducerService kafka;

	@InjectMocks
	private EmployeeServiceImpl service;


	@Test
	@DisplayName("Deve buscar todos os funcionários")
	void findAll() {

		Employee employee = new Employee();
		employee.setId(1L);
		employee.setName("João");

		EmployeeResponseDTO dto = new EmployeeResponseDTO(1L, "João", "Desenvolvedor");

		when(repository.findAll()).thenReturn(List.of(employee));
		when(mapper.toDTO(employee)).thenReturn(dto);

		List<EmployeeResponseDTO> result = service.findAll();

		assertEquals(1, result.size());
		assertEquals("João", result.get(0).name());

		verify(repository).findAll();
		verify(mapper).toDTO(employee);
	}


	@Test
	@DisplayName("Deve buscar funcionário pelo ID")
	void findById() {

		Employee employee = new Employee();
		employee.setId(1L);
		employee.setName("João");

		EmployeeResponseDTO dto = new EmployeeResponseDTO(1L, "João", "Desenvolvedor");

		when(repository.findById(1L)).thenReturn(Optional.of(employee));
		when(mapper.toDTO(employee)).thenReturn(dto);

		EmployeeResponseDTO result = service.findById(1L);

		assertEquals(1L, result.id());
		assertEquals("João", result.name());

		verify(repository).findById(1L);
		verify(mapper).toDTO(employee);
	}


	@Test
	@DisplayName("Deve lançar exceção quando funcionário não existir")
	void findByIdNotFound() {

		when(repository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> service.findById(1L));

		verify(repository).findById(1L);
		verifyNoInteractions(mapper);
	}


	@Test
	@DisplayName("Deve buscar funcionário pelo CPF")
	void findByCpf() {

		Employee employee = new Employee();
		employee.setId(1L);
		employee.setName("João");

		EmployeeDetailsResponseDTO dto = new EmployeeDetailsResponseDTO(
						1L, "João", "12345678909", "Desenvolvedor", LocalDate.of(1995, 1, 1),
						LocalDate.of(2026, 1, 10), Instant.now());

		when(repository.findByCpf("12345678909")).thenReturn(Optional.of(employee));
		when(mapper.toDetailsDTO(employee)).thenReturn(dto);

		EmployeeDetailsResponseDTO result = service.findByCpf("12345678909");

		assertEquals(1L, result.id());
		assertEquals("João", result.name());

		verify(repository).findByCpf("12345678909");
		verify(mapper).toDetailsDTO(employee);
	}


	@Test
	@DisplayName("Deve lançar exceção quando CPF não existir")
	void findByCpfNotFound() {

		when(repository.findByCpf("12345678909")).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> service.findByCpf("12345678909"));

		verify(repository).findByCpf("12345678909");
	}


	@Test
	@DisplayName("Deve buscar funcionários ativos")
	void findAllActive() {

		Employee employee = new Employee();
		employee.setId(1L);
		employee.setActive(true);

		EmployeeResponseDTO dto = new EmployeeResponseDTO(1L, "João", "Dev");

		when(repository.findByActive(true)).thenReturn(List.of(employee));
		when(mapper.toDTO(employee)).thenReturn(dto);

		List<EmployeeResponseDTO> result = service.findAllActive();

		assertEquals(1, result.size());

		verify(repository).findByActive(true);
		verify(mapper).toDTO(employee);
	}


	@Test
	@DisplayName("Deve buscar funcionários inativos")
	void findAllInactive() {

		Employee employee = new Employee();
		employee.setId(1L);
		employee.setActive(false);

		EmployeeResponseDTO dto = new EmployeeResponseDTO(1L, "João", "Dev");

		when(repository.findByActive(false)).thenReturn(List.of(employee));
		when(mapper.toDTO(employee)).thenReturn(dto);

		List<EmployeeResponseDTO> result = service.findAllInactive();

		assertEquals(1, result.size());

		verify(repository).findByActive(false);
		verify(mapper).toDTO(employee);
	}


	@Test
	@DisplayName("Deve criar funcionário e publicar evento de ativação")
	void create() {

		EmployeeRequestDTO request = new EmployeeRequestDTO("João", "12345678909", "Desenvolvedor",
				LocalDate.of(1995, 1, 1), new BigDecimal("150.00"));

		Employee employee = new Employee();
		employee.setId(1L);
		employee.setName("João");

		EmployeeResponseDTO response = new EmployeeResponseDTO(1L, "João", "Desenvolvedor");

		when(repository.existsByCpf(request.cpf())).thenReturn(false);
		when(mapper.toEntity(request)).thenReturn(employee);
		when(repository.save(employee)).thenReturn(employee);
		when(mapper.toDTO(employee)).thenReturn(response);

		EmployeeResponseDTO result = service.create(request);

		assertEquals(1L, result.id());
		assertEquals("João", result.name());

		assertTrue(employee.getActive());
		assertEquals(LocalDate.now(), employee.getHireDate());

		verify(repository).existsByCpf(request.cpf());
		verify(mapper).toEntity(request);
		verify(repository).save(employee);
		verify(kafka).publishEmployeeActivated(any(EmployeeActivatedEvent.class));
		verify(mapper).toDTO(employee);
	}


	@Test
	@DisplayName("Deve impedir cadastro com CPF duplicado")
	void createDuplicateCpf() {

		EmployeeRequestDTO request = new EmployeeRequestDTO("João", "12345678909", "Desenvolvedor",
						LocalDate.of(1995, 1, 1), new BigDecimal("150.00"));

		when(repository.existsByCpf(request.cpf())).thenReturn(true);

		assertThrows(DuplicateCpfException.class, () -> service.create(request));

		verify(repository).existsByCpf(request.cpf());
		verifyNoInteractions(mapper);
		verify(repository, never()).save(any());
		verifyNoInteractions(kafka);
	}


	@Test
	@DisplayName("Deve impedir cadastro de menor de 18 anos")
	void createUnderage() {

		LocalDate birthDate = LocalDate.now().minusYears(17);

		EmployeeRequestDTO request = new EmployeeRequestDTO("João", "12345678909",
						"Desenvolvedor", birthDate, new BigDecimal("150.00"));

		assertThrows(MinimumAgeException.class, () -> service.create(request));

		verifyNoInteractions(repository);
		verifyNoInteractions(mapper);
		verifyNoInteractions(kafka);
	}


	@Test
	@DisplayName("Deve atualizar o cargo e publicar evento")
	void updatePosition() {

		Employee employee = new Employee();
		employee.setId(1L);
		employee.setName("João");
		employee.setPosition("COLABORADOR");

		EmployeePositionRequestDTO request = new EmployeePositionRequestDTO("GERENTE");

		EmployeeResponseDTO response = new EmployeeResponseDTO(1L, "João", "GERENTE");

		when(repository.findById(1L)).thenReturn(Optional.of(employee));
		when(repository.save(employee)).thenReturn(employee);
		when(mapper.toDTO(employee)).thenReturn(response);

		EmployeeResponseDTO result = service.updatePosition(1L, request);

		assertEquals("GERENTE", result.position());

		verify(repository).findById(1L);
		verify(mapper).updatePosition(request, employee);
		verify(repository).save(employee);
		verify(kafka).publishEmployeePositionUpdated(any(EmployeePositionUpdatedEvent.class));
		verify(mapper).toDTO(employee);
	}


	@Test
	@DisplayName("Deve lançar exceção ao tentar alterar funcionário inexistente")
	void updatePositionNotFound() {

		when(repository.findById(1L)).thenReturn(Optional.empty());

		EmployeePositionRequestDTO request = new EmployeePositionRequestDTO("Gerente");

		assertThrows(ResourceNotFoundException.class, () -> service.updatePosition(1L, request));

		verify(repository).findById(1L);
		verify(repository, never()).save(any());
		verifyNoInteractions(mapper);
		verifyNoInteractions(kafka);
	}


	@Test
	@DisplayName("Deve ativar funcionário inativo e publicar evento")
	void activate() {

		Employee employee = new Employee();
		employee.setId(1L);
		employee.setName("João");
		employee.setActive(false);

		EmployeeResponseDTO response = new EmployeeResponseDTO(1L, "João", "Dev");

		when(repository.findById(1L)).thenReturn(Optional.of(employee));
		when(repository.save(employee)).thenReturn(employee);
		when(mapper.toDTO(employee)).thenReturn(response);

		EmployeeResponseDTO result = service.activate(1L);

		assertTrue(employee.getActive());
		assertEquals(1L, result.id());

		verify(repository).findById(1L);
		verify(repository).save(employee);
		verify(kafka).publishEmployeeActivated(any(EmployeeActivatedEvent.class));
		verify(mapper).toDTO(employee);
	}


	@Test
	@DisplayName("Não deve ativar funcionário que já está ativo")
	void activateAlreadyActive() {

		Employee employee = new Employee();
		employee.setId(1L);
		employee.setActive(true);

		when(repository.findById(1L)).thenReturn(Optional.of(employee));

		assertThrows(ActiveException.class, () -> service.activate(1L));

		verify(repository).findById(1L);
		verify(repository, never()).save(any());
		verifyNoInteractions(kafka);
	}


	@Test
	@DisplayName("Deve desativar funcionário ativo e publicar evento")
	void deactivate() {

		Employee employee = new Employee();
		employee.setId(1L);
		employee.setName("João");
		employee.setActive(true);

		EmployeeResponseDTO response = new EmployeeResponseDTO(1L, "João", "Dev");

		when(repository.findById(1L)).thenReturn(Optional.of(employee));
		when(repository.save(employee)).thenReturn(employee);
		when(mapper.toDTO(employee)).thenReturn(response);

		EmployeeResponseDTO result = service.deactivate(1L);

		assertFalse(employee.getActive());
		assertEquals(1L, result.id());

		verify(repository).findById(1L);
		verify(repository).save(employee);
		verify(kafka).publishEmployeeDeactivated(any(EmployeeDeactivatedEvent.class));
		verify(mapper).toDTO(employee);
	}


	@Test
	@DisplayName("Não deve desativar funcionário que já está inativo")
	void deactivateAlreadyInactive() {

		Employee employee = new Employee();
		employee.setId(1L);
		employee.setActive(false);

		when(repository.findById(1L)).thenReturn(Optional.of(employee));

		assertThrows(ActiveException.class, () -> service.deactivate(1L));

		verify(repository).findById(1L);
		verify(repository, never()).save(any());
		verifyNoInteractions(kafka);
	}
}