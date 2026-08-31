package vinix.feignclients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
	    name = "hr-worker",
	    path = "/workers",
	    fallback = WorkerFeignClientFallback.class
	)
	public interface WorkerFeignClient {

	    @GetMapping(value = "/{id}")
	    ResponseEntity<Worker> findById(@PathVariable("id") Long id);
	}
