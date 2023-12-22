package cz.cvut.kbss.study.exception;

/**
 * Indicates that the application is attempting to import a record with a nonexistent author.
 */
public class RecordAuthorNotFoundException extends RecordManagerException {

    public RecordAuthorNotFoundException(String message) {
        super(message);
    }
}
