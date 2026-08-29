package vinix.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import vinix.config.JwtService;
import vinix.feignClient.UserFeignClient;

@Service
public class AuthService implements UserDetailsService {

    private final UserFeignClient userFeignClient;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtUtil;

    @Value("${security.oauth2.client.client-id}")
    private String clientId;

    @Value("${security.oauth2.client.client-secret}")
    private String clientSecret;

    public AuthService(UserFeignClient userFeignClient,
                       BCryptPasswordEncoder passwordEncoder,
                       JwtService jwtUtil) {
        this.userFeignClient = userFeignClient;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    //autenticador
    public String authenticate(String clientId, String clientSecret,
                               String email, String password) {

        // valida client
        if (!this.clientId.equals(clientId) ||
            !this.clientSecret.equals(clientSecret)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Client inválido");
        }

        // busca usuário
        UserDTO user = userFeignClient.findByEmail(email).getBody();

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
        }

        // valida senha
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Senha inválida");
        }

        // gera token
        return jwtUtil.generateToken(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDTO user = userFeignClient.findByEmail(username).getBody();

        if (user == null) {
            throw new UsernameNotFoundException("Email não encontrado: " + username);
        }

        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(
                    user.getRoles().stream()
                        .map(r -> r.getRoleName())
                        .toArray(String[]::new)
                )
                .build();
    }

    public UserDTO findEmail(String email) {
        UserDTO user = userFeignClient.findByEmail(email).getBody();

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
        }

        return user;
    }
}