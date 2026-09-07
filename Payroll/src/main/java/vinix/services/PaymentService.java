package vinix.services;

import org.springframework.security.access.prepost.PreAuthorize;
import vinix.dto.request.PaymentRequestDTO;
import vinix.dto.response.PaymentResponseDTO;
import vinix.entities.PaymentStatus;

import java.util.List;

public interface PaymentService {

  PaymentResponseDTO findById(Long id);

  List<PaymentResponseDTO> findByWorkerId(Long workerId);

  List<PaymentResponseDTO> findAll();

  @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
  List<PaymentResponseDTO> launchPayroll();

  @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
  PaymentResponseDTO create(PaymentRequestDTO dto);

  @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
  PaymentResponseDTO calculate13Salary(PaymentRequestDTO dto);

  @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
  PaymentResponseDTO calculateVacation(PaymentRequestDTO dto);

  @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
  PaymentResponseDTO updateStatus(Long id, PaymentStatus status);

  @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
  PaymentResponseDTO cancel(Long id);
}