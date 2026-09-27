package vinix.services;

import org.springframework.security.access.prepost.PreAuthorize;
import vinix.dto.request.EmployeePositionRequestDTO;
import vinix.dto.request.EmployeeRequestDTO;
import vinix.dto.request.VacationRequestDTO;
import vinix.dto.response.EmployeeDetailsResponseDTO;
import vinix.dto.response.EmployeeResponseDTO;
import vinix.dto.response.EmployeeSalaryResponseDTO;
import vinix.dto.response.VacationResponseDTO;
import vinix.entities.VacationStatus;

import java.util.List;

public interface EmployeeService {

    EmployeeResponseDTO findById(Long id);

    EmployeeSalaryResponseDTO findSalaryById(Long id);

    List<EmployeeResponseDTO> findAll();

    EmployeeDetailsResponseDTO findByCpf(String cpf);

    @PreAuthorize("hasRole('HR')")
    List<EmployeeResponseDTO> findAllActive();

    @PreAuthorize("hasRole('HR')")
    List<EmployeeResponseDTO> findAllInactive();

    @PreAuthorize("isAuthenticated()")
    VacationResponseDTO vacationRequest(VacationRequestDTO dto);

    @PreAuthorize("hasAnyRole('HR', 'MANAGER')")
    VacationResponseDTO vacationApprove(Long id);

    @PreAuthorize("hasAnyRole('HR', 'MANAGER')")
    VacationResponseDTO vacationReject(Long id);

    @PreAuthorize("hasRole('HR')")
    VacationResponseDTO vacationRequestForEmployee(Long employeeId, VacationRequestDTO dto);

    @PreAuthorize("hasRole('HR')")
    EmployeeResponseDTO create(EmployeeRequestDTO dto);

    @PreAuthorize("hasRole('HR')")
    EmployeeResponseDTO updatePosition(Long id, EmployeePositionRequestDTO novaPosition);

    @PreAuthorize("hasRole('HR')")
    EmployeeResponseDTO activate(Long id);

    @PreAuthorize("hasRole('HR')")
    EmployeeResponseDTO deactivate(Long id);
}