package vinix.resource;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import vinix.dto.request.PaymentRequestDTO;
import vinix.dto.request.UpdateStatusRequestDTO;
import vinix.dto.response.PaymentResponseDTO;
import vinix.services.PaymentService;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/payments")
public class PaymentResource {

		private final PaymentService service;


		@GetMapping
		public ResponseEntity<List<PaymentResponseDTO>> findAll() {
			return ResponseEntity.ok(service.findAll());
		}

		@GetMapping(value = "/{id}")
		public ResponseEntity<PaymentResponseDTO> findById(@PathVariable Long id) {
			return ResponseEntity.ok(service.findById(id));
		}

	 @GetMapping(value = "/workerId/{workerId}")
	 public ResponseEntity<List<PaymentResponseDTO>> findByWorkerId(@PathVariable Long workerId) {
			return ResponseEntity.ok(service.findByWorkerId(workerId));
		}

		@PostMapping
		public ResponseEntity<PaymentResponseDTO> create(
				@RequestBody @Valid PaymentRequestDTO dto, UriComponentsBuilder uriBuilder) {

			PaymentResponseDTO response = service.create(dto);
			URI uri = uriBuilder.path("/payments/{id}").buildAndExpand(response.id()).toUri();

			return ResponseEntity.created(uri).body(response);
		}

		@PostMapping(value = "/13salary")
		public ResponseEntity<PaymentResponseDTO> calculate13Salary(
				@RequestBody @Valid PaymentRequestDTO dto, UriComponentsBuilder builder) {

			PaymentResponseDTO response = service.calculate13Salary(dto);
			URI uri = builder.path("/payments/{id}").buildAndExpand(response.id()).toUri();

			return ResponseEntity.created(uri).body(response);
		}

		@PostMapping(value = "/vacation")
		public ResponseEntity<PaymentResponseDTO> calculateVacation(
				@RequestBody @Valid PaymentRequestDTO dto, UriComponentsBuilder builder) {

			PaymentResponseDTO response = service.calculateVacation(dto);
			URI uri = builder.path("/payments/{id}").buildAndExpand(response.id()).toUri();

			return ResponseEntity.created(uri).body(response);
		}

		@PostMapping(value = "/launch")
		public ResponseEntity<List<PaymentResponseDTO>> launchPayroll() {
			return ResponseEntity.ok(service.launchPayroll());
		}

		@PutMapping(value = "/{id}/status")
		public ResponseEntity<PaymentResponseDTO> updateStatus(
				@PathVariable Long id, @RequestBody @Valid UpdateStatusRequestDTO dto) {

			return ResponseEntity.ok(service.updateStatus(id, dto.status()));
		}

		@PutMapping("/{id}/cancel")
		public ResponseEntity<PaymentResponseDTO> cancel(@PathVariable Long id) {
			return ResponseEntity.ok(service.cancel(id));
		}
}

