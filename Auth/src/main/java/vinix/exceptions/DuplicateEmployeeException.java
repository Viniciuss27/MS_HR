package vinix.exceptions;

public class DuplicateEmployeeException extends RuntimeException {
  public DuplicateEmployeeException(String message) {
    super(message);
  }
}
