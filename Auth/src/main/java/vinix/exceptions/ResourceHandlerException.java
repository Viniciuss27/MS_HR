package vinix.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import java.time.Instant;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Slf4j
@RestControllerAdvice
public class ResourceHandlerException {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<StandardError> resourceNotFound(
      ResourceNotFoundException e, HttpServletRequest request) {

    StandardError err = StandardError.builder()
        .timestamp(Instant.now())
        .status(NOT_FOUND.value())// 404
        .error("Resource not found")
        .message(e.getMessage())
        .path(request.getRequestURI()).build();

    return ResponseEntity.status(NOT_FOUND).body(err);
  }

  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<StandardError> usernameNotFound(
      UsernameNotFoundException e, HttpServletRequest request) {

    StandardError err = StandardError.builder()
        .timestamp(Instant.now())
        .status(UNAUTHORIZED.value())//401
        .error("Unauthorized")
        .message("Usuário ou senha inválidos")
        .path(request.getRequestURI()).build();

    return ResponseEntity.status(UNAUTHORIZED).body(err);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<StandardError> accessDenied(
      AccessDeniedException e, HttpServletRequest request) {

    StandardError err = StandardError.builder()
        .timestamp(Instant.now())
        .status(FORBIDDEN.value())//403
        .error("Acesso negado")
        .message("Você não tem permissão para executar esta ação")
        .path(request.getRequestURI()).build();

    return ResponseEntity.status(FORBIDDEN).body(err);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<StandardError> badCredentials(
      BadCredentialsException e, HttpServletRequest request) {

    StandardError err = StandardError.builder()
        .timestamp(Instant.now())
        .status(UNAUTHORIZED.value())//401
        .error("Unauthorized")
        .message("Usuário ou senha inválidos")
        .path(request.getRequestURI()).build();

    return ResponseEntity.status(UNAUTHORIZED).body(err);
  }

  @ExceptionHandler(DuplicateEmailException.class)
  public ResponseEntity<StandardError> duplicateEmail(
      DuplicateEmailException e, HttpServletRequest request) {

    StandardError err = StandardError.builder()
        .timestamp(Instant.now())
        .status(CONFLICT.value())//409
        .message(e.getMessage())
        .error("Email já cadastrado")
        .path(request.getRequestURI()).build();

    return ResponseEntity.status(CONFLICT).body(err);
  }

  @ExceptionHandler(DuplicateEmployeeException.class)
  public ResponseEntity<StandardError> duplicateEmployee(
      DuplicateEmployeeException e, HttpServletRequest request) {

    StandardError err = StandardError.builder()
        .timestamp(Instant.now())
        .status(CONFLICT.value())//409
        .message(e.getMessage())
        .error("Trabalhador já cadastrado")
        .path(request.getRequestURI()).build();

    return ResponseEntity.status(CONFLICT).body(err);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<StandardError> globalException(
      Exception e, HttpServletRequest request) {

    log.error("Erro inesperado no servidor ao acessar {}: ", request.getRequestURI(), e);

    StandardError err = StandardError.builder()
        .timestamp(Instant.now())
        .status(INTERNAL_SERVER_ERROR.value())//500
        .error("Erro Interno no Servidor")
        .message("Ocorreu um erro inesperado. Por favor, tente novamente mais tarde")
        .path(request.getRequestURI()).build();

    return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(err);
  }


  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ValidationError> validation(
      MethodArgumentNotValidException e, HttpServletRequest request) {

    ValidationError err = ValidationError.builder()
        .timestamp(Instant.now())
        .status(UNPROCESSABLE_ENTITY.value())//422
        .error("Validation exception")
        .message("Erro na validação dos campos")
        .path(request.getRequestURI()).build();

    for (FieldError f : e.getBindingResult().getFieldErrors()) {
      err.addErro(f.getField(), f.getDefaultMessage());
    }

    return ResponseEntity.status(UNPROCESSABLE_ENTITY).body(err);
  }
}