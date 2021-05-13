package cz.cvut.kbss.study.exception;

/**
 * High-level exception marking a validated object invalid.
 */
public class ValidationException extends FormManagerException {

    private final String messageId;

    public ValidationException(String messageId, String message) {
        super(message);
        this.messageId = messageId;
    }

    public ValidationException(String message) {
        super(message);
        this.messageId = null;
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        messageId = null;
    }

    public ValidationException(Throwable cause) {
        super(cause);
        messageId = null;
    }

    public String getMessageId() {
        return messageId;
    }
}
