package vinix.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(
	    name = "employee",
	    path = "/emplyees",
	    fallbackFactory = EmployeeFeignClientFallbackFactory.class
	)
	public interface EmployeeFeignClient {

	    @GetMapping(value = "/{id}")
	    ResponseEntity<EmployeeDTO> findById(@PathVariable Long id);

					@GetMapping
	    ResponseEntity<List<EmployeeDTO>> findAllActive();
	}
