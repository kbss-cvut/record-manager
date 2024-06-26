package cz.cvut.kbss.study.exception;

/**
 * Exception thrown when access to other application's web services fails.
 */
public class WebServiceIntegrationException extends RecordManagerException {

    public WebServiceIntegrationException(String message) {
        super(message);
    }

    public WebServiceIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
