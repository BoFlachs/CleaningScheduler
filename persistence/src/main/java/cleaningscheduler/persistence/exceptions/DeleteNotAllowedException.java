package cleaningscheduler.persistence.exceptions;

public class DeleteNotAllowedException extends RuntimeException {
    public DeleteNotAllowedException(String message) {
        super(message);
    }
}
