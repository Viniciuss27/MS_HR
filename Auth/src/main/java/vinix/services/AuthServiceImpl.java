package vinix.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vinix.config.JwtService;
import vinix.dto.request.LoginRequestDTO;
import vinix.dto.request.RegisterRequestDTO;
import vinix.dto.response.LoginResponseDTO;
import vinix.dto.response.UserResponseDTO;
import vinix.entities.Role;
import vinix.entities.User;
import vinix.exceptions.DuplicateEmployeeException;
import vinix.mapper.UserMapper;
import vinix.repositories.RoleRepository;
import vinix.repositories.UserRepository;
import vinix.exceptions.DuplicateEmailException;
import vinix.exceptions.ResourceNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

			private final AuthenticationManager autenticador;
			private final UserRepository userRepository;
			private final RoleRepository roleRepository;
			private final PasswordEncoder password;
			private final UserMapper mapper;
			private final JwtService jwtService;

			@Override
			@Transactional(readOnly = true)
			public LoginResponseDTO login(LoginRequestDTO dto) {
						autenticador.authenticate(
								new UsernamePasswordAuthenticationToken(dto.email(),
										dto.password()));

						User user = userRepository.findByEmail(dto.email())
								.orElseThrow(() -> new ResourceNotFoundException(
										"Usuário não encontrado"));

						List<String> roles = user.getRoles().stream()
								.map(Role::getRoleName).toList();

						String token = jwtService.generateToken(user.getEmail(), roles);

						return new LoginResponseDTO(token, "Bearer",
								jwtService.getExpiration());
			}


			@Override
			@Transactional
			@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
			public UserResponseDTO register(RegisterRequestDTO dto) {

				if (userRepository.findByEmail(dto.email()).isPresent()) {
					throw new DuplicateEmailException("Já existe uma conta cadastrada com o email " + dto.email());
				}

				if (userRepository.findByEmployeeId(dto.employeeId()).isPresent()) {
					throw new DuplicateEmployeeException("Já existe uma conta cadastrada para o funcionário " + dto.employeeId());
				}

				Role role = roleRepository.findByRoleName("USER")
						.orElseThrow(() -> new ResourceNotFoundException("Role padrão não encontrada: USER"));

				User user = mapper.toEntity(dto);
				user.setPassword(password.encode(dto.password()));
				user.getRoles().add(role);

				return mapper.toResponseDTO(userRepository.save(user));
			}
}