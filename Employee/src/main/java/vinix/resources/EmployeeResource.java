package vinix.resources;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import vinix.dto.request.EmployeePositionRequestDTO;
import vinix.dto.request.EmployeeRequestDTO;
import vinix.dto.request.VacationRequestDTO;
import vinix.dto.response.EmployeeDetailsResponseDTO;
import vinix.dto.response.EmployeeResponseDTO;
import vinix.dto.response.EmployeeSalaryResponseDTO;
import vinix.dto.response.VacationResponseDTO;
import vinix.services.EmployeeService;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/employees")
public class EmployeeResource {

  private final EmployeeService service;

  @GetMapping
  public ResponseEntity<List<EmployeeResponseDTO>> findAll() {
    return ResponseEntity.ok(service.findAll());
  }

  @GetMapping(value = "/{id}/salary")
  public ResponseEntity<EmployeeSalaryResponseDTO> findSalaryById(@PathVariable Long id) {
    return ResponseEntity.ok(service.findSalaryById(id));
  }

  @GetMapping(value = "/me")
  public ResponseEntity<EmployeeDetailsResponseDTO> findMe() {
    return ResponseEntity.ok(service.findMe());
  }

  @GetMapping(value = "/{id}")
  public ResponseEntity<EmployeeResponseDTO> findById(@PathVariable Long id) {
    return ResponseEntity.ok(service.findById(id));
  }

  @GetMapping("/salary/active")
  public ResponseEntity<List<EmployeeSalaryResponseDTO>> findAllActiveSalary() {
    return ResponseEntity.ok(service.findAllActiveSalary());
  }

  @GetMapping(value = "/active")
  public ResponseEntity<List<EmployeeResponseDTO>> findAllActive() {
    return ResponseEntity.ok(service.findAllActive());
  }

  @GetMapping(value = "/inactive")
  public ResponseEntity<List<EmployeeResponseDTO>> findAllInactive() {
    return ResponseEntity.ok(service.findAllInactive());
  }

  @GetMapping("/vacation-requests/me")
  public ResponseEntity<List<VacationResponseDTO>> findMyVacations() {
    return ResponseEntity.ok(service.findMyVacations());
  }

  @PostMapping("/vacation-requests")
  public ResponseEntity<VacationResponseDTO> vacationRequest(@RequestBody @Valid VacationRequestDTO dto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.vacationRequest(dto));
  }

  @PostMapping("/vacation-requests/{employeeId}")
  public ResponseEntity<VacationResponseDTO> vacationRequestForEmployee(
      @PathVariable Long employeeId, @RequestBody @Valid VacationRequestDTO dto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.vacationRequestForEmployee(employeeId, dto));
  }

  @PostMapping
  public ResponseEntity<EmployeeResponseDTO> create(
      @RequestBody @Valid EmployeeRequestDTO dto, UriComponentsBuilder builder) {

    EmployeeResponseDTO response = service.create(dto);
    URI uri = builder.path("/employees/{id}").buildAndExpand(response.id()).toUri();
    return ResponseEntity.created(uri).body(response);
  }

  @PutMapping(value = "/vacation-requests/{id}/approve")
  public ResponseEntity<VacationResponseDTO> vacationApprove(@PathVariable Long id) {
    return ResponseEntity.ok(service.vacationApprove(id));
  }

  @PutMapping(value = "/vacation-requests/{id}/reject")
  public ResponseEntity<VacationResponseDTO> vacationReject(@PathVariable Long id) {
    return ResponseEntity.ok(service.vacationReject(id));
  }

  @PutMapping(value = "/{id}/update")
  public ResponseEntity<EmployeeResponseDTO> updatePosition(
      @PathVariable Long id, @RequestBody @Valid EmployeePositionRequestDTO newPosition) {
    return ResponseEntity.ok(service.updatePosition(id, newPosition));
  }

  @PutMapping(value = "/{id}/deactivate")
  public ResponseEntity<EmployeeResponseDTO> deactivate(@PathVariable Long id) {
    return ResponseEntity.ok(service.deactivate(id));
  }

  @PutMapping(value = "/{id}/activate")
  public ResponseEntity<EmployeeResponseDTO> activate(@PathVariable Long id) {
    return ResponseEntity.ok(service.activate(id));
  }
}
