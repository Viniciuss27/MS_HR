package vinix.resources.exceptions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@SuperBuilder
@Getter
@NoArgsConstructor
public class StandardError {

  private Instant timestamp;
  private Integer status;
  private String message;
  private String error;
  private String path;
}
