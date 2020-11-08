package cz.cvut.kbss.study.exception;

/**
 * Application-specific exception.
 * <p>
 * All exceptions related to the application should be subclasses of this one.
 */
public class FormManagerException extends RuntimeException {

    protected FormManagerException() {
    }

    public FormManagerException(String message) {
        super(message);
    }

    public FormManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public FormManagerException(Throwable cause) {
        super(cause);
    }
}
