package vinix.feignclients;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class WorkerFeignClientFallback implements WorkerFeignClient{

	@Override
	public ResponseEntity<Worker> findById(Long id){
		Worker worker = new Worker(id, "Fallback", 0.0);
        return ResponseEntity.ok(worker);
	}
	
}
