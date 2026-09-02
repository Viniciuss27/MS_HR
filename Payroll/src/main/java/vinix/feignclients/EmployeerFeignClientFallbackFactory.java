package vinix.feignclients;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Scheduler.Worker;

import java.util.List;

@Component
@Slf4j
public class EmployeerFeignClientFallbackFactory implements FallbackFactory<EmployeerFeignClient> {

	public EmployeerFeignClient create(Throwable cause) {
		return new EmployeerFeignClient() {
			@Override
			public ResponseEntity<EmployeerDTO> findById(Long id) {
				log.error("Não foi possível buscar o ID: {}, motivo: {}", id, cause.getMessage());
				return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
			}

			@Override
			public ResponseEntity<List<EmployeerDTO>> findAllActive() {
				log.error("Não foi possível buscar a lista de funcionários, motivo: {}", cause.getMessage());
				return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
			}
		};
	}
}
