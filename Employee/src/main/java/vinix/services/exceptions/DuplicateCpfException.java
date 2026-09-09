package vinix.services.exceptions;

public class DuplicateCpfException extends RuntimeException {
  public DuplicateCpfException(String message) {
    super(message);
  }
}
