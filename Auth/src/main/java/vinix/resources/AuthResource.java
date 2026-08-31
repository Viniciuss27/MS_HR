package vinix.resources;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import vinix.dto.request.LoginRequestDTO;
import vinix.dto.request.RegisterRequestDTO;
import vinix.dto.response.LoginResponseDTO;
import vinix.dto.response.UserResponseDTO;
import vinix.services.AuthService;


import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/auth")
public class AuthResource {

    private final AuthService service;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO dto) {
        return ResponseEntity.ok(service.login(dto));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@RequestBody @Valid RegisterRequestDTO dto) {
        UserResponseDTO response = service.register(dto);
        URI uri = ServletUriComponentsBuilder
            .fromCurrentRequest().path("/{id}")
            .buildAndExpand(response.id())
            .toUri();
        return ResponseEntity.created(uri).body(response);
    }
}