package vinix.dto.response;

public record LoginResponseDTO(
	    String token,
	    String tokenType,
	    long expiresIn
	) {}