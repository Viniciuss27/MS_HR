package vinix.feignclients;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Scheduler.Worker;

@Component
@Slf4j
public class EmployeerFeignClientFallbackFactory implements FallbackFactory<EmployeerFeignClient> {

			@Override
			public EmployeerFeignClient create(Throwable cause) {
				  return (Long id) -> {
							log.error("Não foi possivel buscar o ID: {}, motivo: {}",
									id, cause.getMessage());
							return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
				  };
			}
}
