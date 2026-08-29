package vinix.feignClient;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import vinix.dto.UserDTO;

@Component
public class UserFeignClientFallback implements UserFeignClient{

	@Override
	public ResponseEntity<UserDTO> findByEmail(String email) {
		return ResponseEntity.ok(new UserDTO(null, "Fallback" , email, null));
	}

}
