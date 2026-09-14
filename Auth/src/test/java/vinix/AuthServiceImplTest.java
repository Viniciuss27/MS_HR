package vinix;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import vinix.config.JwtService;
import vinix.dto.request.LoginRequestDTO;
import vinix.dto.request.RegisterRequestDTO;
import vinix.dto.response.LoginResponseDTO;
import vinix.dto.response.UserResponseDTO;
import vinix.entities.Role;
import vinix.entities.User;
import vinix.exceptions.DuplicateEmailException;
import vinix.exceptions.DuplicateEmployeeException;
import vinix.exceptions.ResourceNotFoundException;
import vinix.mapper.UserMapper;
import vinix.repositories.RoleRepository;
import vinix.repositories.UserRepository;
import vinix.services.AuthServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl - testes Unitários")
public class AuthServiceImplTest {

    @Mock private AuthenticationManager autenticador;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder password;
    @Mock private UserMapper mapper;
    @Mock private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl service;

    private LoginRequestDTO loginRequest;
    private RegisterRequestDTO registerRequest;
    private User user;
    private Role roleUser;
    private UserResponseDTO userResponseDTO;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequestDTO("funcionario@hr.com", "senha123");

        registerRequest = new RegisterRequestDTO(
            "Funcionário Teste", "funcionario@hr.com", "senha123", 1L);

        roleUser = new Role();
        roleUser.setId(1L);
        roleUser.setRoleName("USER");

        user = new User(1L, "Funcionário Teste", "funcionario@hr.com", "hashDaSenha", 1L);
        user.getRoles().add(roleUser);
        userResponseDTO = new UserResponseDTO(
            1L, "Funcionário Teste", "funcionario@hr.com", 1L, true, List.of("USER"));
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("Deve autenticar e retornar token quando credenciais forem válidas")
        void autenticaComSucesso() {

            when(userRepository.findByEmail("funcionario@hr.com")).thenReturn(Optional.of(user));
            when(jwtService.generateToken("funcionario@hr.com", List.of("USER"))).thenReturn("token-fake-jwt");
            when(jwtService.getExpiration()).thenReturn(86400000L);

            LoginResponseDTO resultado = service.login(loginRequest);

            assertThat(resultado.token()).isEqualTo("token-fake-jwt");
            assertThat(resultado.tokenType()).isEqualTo("Bearer");

            verify(autenticador).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }

        @Test
        @DisplayName("Deve propagar BadCredentialsException quando a senha estiver errada")
        void senhaErrada() {

            doThrow(new BadCredentialsException("Credenciais inválidas")).when(autenticador).authenticate(any());

            assertThatThrownBy(() -> service.login(loginRequest)).isInstanceOf(BadCredentialsException.class);

            verifyNoInteractions(userRepository, jwtService);
        }

        @Test
        @DisplayName("Deve lançar exceção quando o usuário autenticado não for encontrado no banco")
        void usuarioNaoEncontrado() {

            when(userRepository.findByEmail("funcionario@hr.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.login(loginRequest)).isInstanceOf(ResourceNotFoundException.class);

            verifyNoInteractions(jwtService);
        }
    }

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("Deve registrar o usuário quando email e funcionário não estiverem cadastrados")
        void registraComSucesso() {

            when(userRepository.findByEmail("funcionario@hr.com")).thenReturn(Optional.empty());
            when(userRepository.findByEmployeeId(1L)).thenReturn(Optional.empty());
            when(roleRepository.findByRoleName("USER")).thenReturn(Optional.of(roleUser));
            when(mapper.toEntity(registerRequest)).thenReturn(user);
            when(password.encode("senha123")).thenReturn("hashDaSenha");
            when(userRepository.save(user)).thenReturn(user);
            when(mapper.toResponseDTO(user)).thenReturn(userResponseDTO);

            UserResponseDTO resultado = service.register(registerRequest);

            assertThat(resultado.email()).isEqualTo("funcionario@hr.com");
            assertThat(resultado.employeeId()).isEqualTo(1L);
            assertThat(resultado.active())
                .isTrue();

            assertThat(resultado.roles()).contains("USER");

            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Deve lançar DuplicateEmailException quando o email já existir")
        void emailJaExiste() {

            when(userRepository.findByEmail("funcionario@hr.com")).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> service.register(registerRequest))
                .isInstanceOf(DuplicateEmailException.class).hasMessageContaining("funcionario@hr.com");

            verifyNoInteractions(roleRepository);
            verify(userRepository, never()).findByEmployeeId(any());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar DuplicateEmployeeException quando o funcionário já possuir uma conta")
        void funcionarioJaPossuiConta() {

            when(userRepository.findByEmail("funcionario@hr.com")).thenReturn(Optional.empty());
            when(userRepository.findByEmployeeId(1L)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> service.register(registerRequest))
                .isInstanceOf(DuplicateEmployeeException.class).hasMessageContaining("1");

            verifyNoInteractions(roleRepository);
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando a role padrão USER não existir")
        void rolePadraoNaoExiste() {

            when(userRepository.findByEmail("funcionario@hr.com")).thenReturn(Optional.empty());
            when(userRepository.findByEmployeeId(1L)).thenReturn(Optional.empty());
            when(roleRepository.findByRoleName("USER")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.register(registerRequest))
                .isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("USER");

            verify(userRepository, never()).save(any());
        }
    }
}