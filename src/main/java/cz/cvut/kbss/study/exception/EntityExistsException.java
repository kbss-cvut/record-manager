package cz.cvut.kbss.study.exception;

public class EntityExistsException extends RecordManagerException {

    public EntityExistsException(String message) {
        super(message);
    }
}
