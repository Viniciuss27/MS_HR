package vinix.resource.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.Instant;

@SuperBuilder
@NoArgsConstructor
@Getter
public class StandardError implements Serializable {
  private static final long serialVersionUID = 1L;

  private Instant timestamp;
  private Integer status;
  private String error;
  private String message;
  private String path;
}