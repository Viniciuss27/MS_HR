package vinix.resource.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@SuperBuilder
@NoArgsConstructor
@Getter
public class ValidationError extends StandardError{
  private static final long serialVersionUID = 1L;

  @Builder.Default
  private List<FileMensage> errors = new ArrayList<>();

  public void addError(String name, String message) {
    errors.add(new FileMensage(name, message));
  }

  public record FileMensage(String name, String message){}
}
