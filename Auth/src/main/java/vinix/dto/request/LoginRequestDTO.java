package vinix.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
		   @NotBlank(message = "O email é obrigatório")
	    @Email(message = "Digite um email valido")
	    String email,
	    
	    @NotBlank (message = "A senha é obrigatório")
	    String password
	) {}