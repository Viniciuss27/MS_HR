package vinix.services;

import org.springframework.security.access.prepost.PreAuthorize;
import vinix.dto.request.EmployeePositionRequestDTO;
import vinix.dto.request.EmployeeRequestDTO;
import vinix.dto.response.EmployeeDetailsResponseDTO;
import vinix.dto.response.EmployeeResponseDTO;

import java.util.List;

public interface EmployeeService {

    EmployeeResponseDTO findById(Long id);

    List<EmployeeResponseDTO> findAll();

    EmployeeDetailsResponseDTO findByCpf(String cpf);

    @PreAuthorize("hasRole('HR')")
    List<EmployeeResponseDTO> findAllActive();

    @PreAuthorize("hasRole('HR')")
    List<EmployeeResponseDTO> findAllInactive();

    @PreAuthorize("hasRole('HR')")
    EmployeeResponseDTO create(EmployeeRequestDTO dto);

    @PreAuthorize("hasRole('HR')")
    EmployeeResponseDTO updatePosition(Long id, EmployeePositionRequestDTO novaPosition);

    @PreAuthorize("hasRole('HR')")
    EmployeeResponseDTO activate(Long id);

    @PreAuthorize("hasRole('HR')")
    EmployeeResponseDTO deactivate(Long id);
}