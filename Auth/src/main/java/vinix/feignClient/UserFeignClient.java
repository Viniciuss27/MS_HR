package vinix.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import vinix.dto.UserDTO;

@FeignClient(
		name = "hr-user",
		path = "/users",
		fallback = UserFeignClientFallback.class)
public interface UserFeignClient {

	@GetMapping(value = "/email/{email}")
	ResponseEntity<UserDTO> findByEmail(@PathVariable String email);
	
}
