package vinix.config;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import vinix.dto.RoleDTO;
import vinix.dto.UserDTO;
@Component
public class JwtService {

    private final SecretKey secretKey;
    private final Long expiration;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration}") Long expiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    public String generateToken(UserDTO user, Set<RoleDTO> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(user.getEmail())
            .claim("roles", roles.stream().map(RoleDTO::getRoleName).collect(Collectors.toList()))
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(expiration, ChronoUnit.MILLIS)))
            .signWith(secretKey)
            .compact();
    }
}