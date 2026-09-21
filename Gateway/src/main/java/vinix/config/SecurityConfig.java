package vinix.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

		@Value("${jwt.secret}")
		private String secret;

		private static final String[] PUBLIC = {
				"/auth/login",
				"/v3/api-docs/**",
				"/swagger-ui/**",
				"/webjars/**"
		};

		private static final String[] REGISTER = {
				"/auth/register"
		};

		@Bean
		ReactiveJwtDecoder jwtDecoder() {
			SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
			return NimbusReactiveJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
		}

		@Bean
		ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter() {

			JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();

			authorities.setAuthoritiesClaimName("roles");
			authorities.setAuthorityPrefix("ROLE_");

			JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

			converter.setJwtGrantedAuthoritiesConverter(authorities);

			return new ReactiveJwtAuthenticationConverterAdapter(converter);
		}

		@Bean
		SecurityWebFilterChain securityWebFilterChain(
				ServerHttpSecurity http,
				ReactiveJwtAuthenticationConverterAdapter converter,
				ReactiveJwtDecoder jwtDecoder) {

			return http
					.csrf(ServerHttpSecurity.CsrfSpec::disable)
					.httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
					.formLogin(ServerHttpSecurity.FormLoginSpec::disable)

					.authorizeExchange(exchange -> exchange
							.pathMatchers(PUBLIC).permitAll()
							.pathMatchers(REGISTER).hasAnyRole("ADMIN", "HR")
							.anyExchange().authenticated())

					.oauth2ResourceServer(oauth2 -> oauth2.jwt(
							jwt -> jwt.jwtDecoder(jwtDecoder)
									.jwtAuthenticationConverter(converter)))

					.build();
		}
}