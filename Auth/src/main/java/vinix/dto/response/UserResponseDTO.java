package vinix.dto.response;

import java.util.List;

public record UserResponseDTO(
	    Long id,
	    String name,
	    String email,
	    List<String> roles
	) {}