package vinix.services;

import vinix.dto.request.LoginRequestDTO;
import vinix.dto.request.RegisterRequestDTO;
import vinix.dto.response.LoginResponseDTO;
import vinix.dto.response.UserResponseDTO;

public interface AuthService {
    LoginResponseDTO login (LoginRequestDTO dto);
    UserResponseDTO register(RegisterRequestDTO dto);
}