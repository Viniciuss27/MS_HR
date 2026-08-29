package vinix.resources;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vinix.dto.TokenResponse;
import vinix.dto.UserDTO;
import vinix.services.AuthService;

@RestController
@RequestMapping(value = "/oauth")
public class AuthResource {

    private final AuthService authService;
    //injeção por construtor (boa prática, evita @Autowired em campo)

    public AuthResource(AuthService authService) {
        this.authService = authService;
    }

    //Responsável por autenticar e gerar o token JWT
    @PostMapping(value = "/token")
    public ResponseEntity<TokenResponse> login(
            @RequestHeader("client-id") String clientId,
            @RequestHeader("client-secret") String clientSecret,
            @RequestParam String email, 
            @RequestParam String password) {

        String token = authService.authenticate(clientId, clientSecret, email, password);

        //retorna no padrão OAuth-like
        return ResponseEntity.ok(
                new TokenResponse(token, "bearer")
        );
    }

    //Opcional para teste de comunicação com o user-service
    //controller não acessa Feign direto, usa o service
    @GetMapping(value = "/user")
    public ResponseEntity<UserDTO> getUser(@RequestParam String email) {

        //service faz a busca e tratamento de erro (404 se não encontrar)
        UserDTO user = authService.findEmail(email);

        return ResponseEntity.ok(user);
    }
}