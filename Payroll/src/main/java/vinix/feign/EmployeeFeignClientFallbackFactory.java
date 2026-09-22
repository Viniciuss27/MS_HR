package vinix.feign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class EmployeeFeignClientFallbackFactory implements FallbackFactory<EmployeeFeignClient> {

	public EmployeeFeignClient create(Throwable cause) {
		return new EmployeeFeignClient() {
			@Override
			public ResponseEntity<EmployeeDTO> findById(Long id, String authorization) {
				log.error("Não foi possível buscar o ID: {}, motivo: {}", id, cause.getMessage());
				return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
			}

			@Override
			public ResponseEntity<List<EmployeeDTO>> findAllActive(String authorization) {
				log.error("Não foi possível buscar a lista de funcionários, motivo: {}", cause.getMessage());
				return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
			}
		};
	}
}
