package vinix.resources.exceptions;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@SuperBuilder
@Getter
@NoArgsConstructor
public class ValidationError extends StandardError {
  private static final long serialVersionUID = 1L;

  @Builder.Default
  private List<FileMessage> errors = new ArrayList<>();

  public void addError(String name, String message) {
    errors.add(new FileMessage(name, message));
  }

  public record FileMessage(String name, String message) {}
}
