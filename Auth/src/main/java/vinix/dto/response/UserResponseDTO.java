package vinix.dto.response;

import java.util.List;

public record UserResponseDTO(
			Long id,
			String name,
			String email,
			Long employeeId,
			Boolean active,
			List<String> roles
	) {}