package cz.cvut.kbss.study.exception;

/**
 * General exception marking an error in the persistence layer.
 */
public class PersistenceException extends RecordManagerException {

    public PersistenceException(String message) {
        super(message);
    }

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public PersistenceException(Throwable cause) {
        super(cause);
    }
}
