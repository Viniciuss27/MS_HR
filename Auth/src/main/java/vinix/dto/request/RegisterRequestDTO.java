package vinix.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
		   @NotBlank(message = "O nome é obrigatório")
	    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
	    String name,
	    
	    @NotBlank (message = "O email é obrigatório")
	    @Email(message = "Digite um email valido")
	    String email,
	    
	    @NotBlank (message = "A senha é obrigatório")
		   @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
	    String password
	) {}