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

    @PreAuthorize("hasRole('HR')")
    EmployeeResponseDTO findById(Long id);

    @PreAuthorize("isAuthenticated()")
    EmployeeDetailsResponseDTO findMe();

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    EmployeeSalaryResponseDTO findSalaryById(Long id);

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    List<EmployeeResponseDTO> findAll();

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    List<EmployeeSalaryResponseDTO> findAllActiveSalary();

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    List<EmployeeResponseDTO> findAllActive();

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    List<EmployeeResponseDTO> findAllInactive();

    @PreAuthorize("isAuthenticated()")
    VacationResponseDTO vacationRequest(VacationRequestDTO dto);

    @PreAuthorize("isAuthenticated()")
    List<VacationResponseDTO> findMyVacations();

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