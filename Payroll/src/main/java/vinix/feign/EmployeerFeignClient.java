package vinix.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(
	    name = "employeer",
	    path = "/emplyeers",
	    fallbackFactory = EmployeerFeignClientFallbackFactory.class
	)
	public interface EmployeerFeignClient {

	    @GetMapping(value = "/{id}")
	    ResponseEntity<EmployeerDTO> findById(@PathVariable Long id);

					@GetMapping
	    ResponseEntity<List<EmployeerDTO>> findAllActive();
	}
