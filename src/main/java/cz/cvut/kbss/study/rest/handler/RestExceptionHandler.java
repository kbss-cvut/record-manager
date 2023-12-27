package cz.cvut.kbss.study.rest.handler;

import cz.cvut.kbss.study.exception.EntityExistsException;
import cz.cvut.kbss.study.exception.NotFoundException;
import cz.cvut.kbss.study.exception.PersistenceException;
import cz.cvut.kbss.study.exception.RecordAuthorNotFoundException;
import cz.cvut.kbss.study.exception.ValidationException;
import cz.cvut.kbss.study.exception.WebServiceIntegrationException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Exception handlers for REST controllers.
 * <p>
 * The general pattern should be that unless an exception can be handled in a more appropriate place it bubbles up to a
 * REST controller which originally received the request. There, it is caught by this handler, logged and a reasonable
 * error message is returned to the user.
 */
@ControllerAdvice
public class RestExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorInfo> resourceNotFound(HttpServletRequest request, NotFoundException e) {
        logException(request, e);
        return new ResponseEntity<>(errorInfo(request, e), HttpStatus.NOT_FOUND);
    }

    private ErrorInfo errorInfo(HttpServletRequest request, Throwable e) {
        return ErrorInfo.createWithMessage(e.getMessage(), request.getRequestURI());
    }

    private ErrorInfo errorInfo(HttpServletRequest request, ValidationException e) {
        return ErrorInfo.createWithMessageAndMessageId(
                e.getMessageId(),
                e.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorInfo> invalidReport(HttpServletRequest request, ValidationException e) {
        logException(request, e);
        return new ResponseEntity<>(errorInfo(request, e), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorInfo> mappingException(HttpServletRequest request, HttpMessageNotReadableException e) {
        logException(request, e);
        return new ResponseEntity<>(errorInfo(request, e.getCause()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<ErrorInfo> entityExistsException(HttpServletRequest request, EntityExistsException e) {
        logException(request, e);
        return new ResponseEntity<>(errorInfo(request, e), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(WebServiceIntegrationException.class)
    public ResponseEntity<ErrorInfo> webServiceIntegrationException(HttpServletRequest request,
                                                                    WebServiceIntegrationException e) {
        logException(request, e);
        return new ResponseEntity<>(errorInfo(request, e), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ErrorInfo> unsupportedOperationException(HttpServletRequest request,
                                                                   UnsupportedOperationException e) {
        logException(request, e);
        return new ResponseEntity<>(errorInfo(request, e), HttpStatus.NOT_IMPLEMENTED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorInfo> illegalArgumentException(HttpServletRequest request, IllegalArgumentException e) {
        logException(request, e);
        return new ResponseEntity<>(errorInfo(request, e), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PersistenceException.class)
    public ResponseEntity<ErrorInfo> persistenceException(HttpServletRequest request, PersistenceException e) {
        logException(request, e);
        return new ResponseEntity<>(errorInfo(request, e.getCause()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RecordAuthorNotFoundException.class)
    public ResponseEntity<ErrorInfo> recordAuthorNotFoundException(HttpServletRequest request,
                                                                   RecordAuthorNotFoundException e) {
        logException(request, e);
        return new ResponseEntity<>(errorInfo(request, e), HttpStatus.CONFLICT);
    }

    void logException(HttpServletRequest request, RuntimeException e) {
        LOG.debug("Request to '{}' failed due to error: {}", request.getRequestURI(), e.getMessage());
    }
}
