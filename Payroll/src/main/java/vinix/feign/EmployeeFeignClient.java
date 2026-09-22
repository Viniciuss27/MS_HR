package vinix.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(
		name = "employee",
		path = "/employees",
		fallbackFactory = EmployeeFeignClientFallbackFactory.class
)
public interface EmployeeFeignClient {

	@GetMapping(value = "/{id}/salary")
	ResponseEntity<EmployeeDTO> findById(@PathVariable Long id,
	  @RequestHeader("Authorization") String authorization);

	@GetMapping
	ResponseEntity<List<EmployeeDTO>> findAllActive(
			@RequestHeader("Authorization") String authorization);
}
