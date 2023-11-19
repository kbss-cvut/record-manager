package cz.cvut.kbss.study.exception;

/**
 * Application-specific exception.
 * <p>
 * All exceptions related to the application should be subclasses of this one.
 */
public class RecordManagerException extends RuntimeException {

    protected RecordManagerException() {
    }

    public RecordManagerException(String message) {
        super(message);
    }

    public RecordManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public RecordManagerException(Throwable cause) {
        super(cause);
    }
}
