package vinix.resource.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vinix.services.exceptions.ResourceNotFoundException;
import vinix.services.exceptions.ServicoIndisponivelException;

import java.time.Instant;


import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@RestControllerAdvice
@Slf4j
public class ResourceExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<StandardError> resourceNotFound(
      ResourceNotFoundException e, HttpServletRequest request) {

    StandardError err = StandardError.builder()
        .timestamp(Instant.now())
        .status(NOT_FOUND.value())//404
        .message(e.getMessage())
        .error("Recurso não encontrado")
        .path(request.getRequestURI()).build();

    return ResponseEntity.status(NOT_FOUND).body(err);
  }
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<StandardError> existenterror(
      IllegalStateException e, HttpServletRequest request) {

    StandardError err = StandardError.builder()
        .timestamp(Instant.now())
        .status(CONFLICT.value())//409
        .message(e.getMessage())
        .error("Regra de negócio violada")
        .path(request.getRequestURI()).build();

    return ResponseEntity.status(CONFLICT).body(err);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<StandardError> authorizationDenied(
      AccessDeniedException e, HttpServletRequest request) {

    StandardError error = StandardError.builder()
        .timestamp(Instant.now())
        .status(FORBIDDEN.value())//403
        .error("Acesso negado")
        .message("Você não possui permissão para realizar esta operação")
        .path(request.getRequestURI()).build();

    return ResponseEntity.status(FORBIDDEN).body(error);
  }

  @ExceptionHandler(ServicoIndisponivelException.class)
  public ResponseEntity<StandardError> servicoIndisponivel(
      ServicoIndisponivelException e, HttpServletRequest request) {

    StandardError err = StandardError.builder()
        .timestamp(Instant.now())
        .status(SERVICE_UNAVAILABLE.value())//503
        .message(e.getMessage())
        .error("Serviço Indisponível")
        .path(request.getRequestURI()).build();

    return ResponseEntity.status(SERVICE_UNAVAILABLE).body(err);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<StandardError> dataIntegrity(
      DataIntegrityViolationException e, HttpServletRequest request) {

    log.error("Violação de integridade de dados: {}", e.getMessage());

    StandardError err = StandardError.builder()
        .timestamp(Instant.now())
        .status(CONFLICT.value())//409
        .error("Violação de integridade de dados")
        .message("Não é possível realizar esta operação pois o registro possui relacionamentos vinculados")
        .path(request.getRequestURI()).build();

    return ResponseEntity.status(CONFLICT).body(err);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<StandardError> globalException(
      Exception e, HttpServletRequest request) {

    log.error("Erro inesperado no servidor ao acessar {}: ",
        request.getRequestURI(), e);

    StandardError err = StandardError.builder()
        .timestamp(Instant.now())
        .status(INTERNAL_SERVER_ERROR.value())
        .error("Erro Interno no Servidor")
        .message("Ocorreu um erro inesperado, Por favor, tente novamente mais tarde")
        .path(request.getRequestURI()).build();

    return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(err);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ValidationError> validation(
      MethodArgumentNotValidException e, HttpServletRequest request){

    ValidationError error = ValidationError.builder()
        .timestamp(Instant.now())
        .status(UNPROCESSABLE_ENTITY.value())//422
        .error("Validation exception")
        .message("Erro na validação dos campos")
        .path(request.getRequestURI()).build();

    for(FieldError f : e.getBindingResult().getFieldErrors()){
      error.addError(f.getField(), f.getDefaultMessage());
    }

    return ResponseEntity.status(UNPROCESSABLE_ENTITY).body(error);
  }
}
