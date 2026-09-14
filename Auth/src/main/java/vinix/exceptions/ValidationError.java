package vinix.exceptions;

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

  @Builder.Default
  private List<FieldMessage> erros = new ArrayList<>();

  public void addErro(String fieldName, String message ) {
    erros.add(new FieldMessage(fieldName ,message));
  }

  public record FieldMessage(String fieldName, String message) {}
}

