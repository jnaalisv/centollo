package centollo.web.interfaces;

import centollo.model.application.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public final class RestErrorHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(RestErrorHandler.class);

    /**
     * Override handling for standard Spring MVC exceptions.
     *
     * @param ex throwable
     * @param body this is null ny default
     * @param headers http headers
     * @param status http status set by Spring to be returned
     * @param request client request
     * @return ResponseEntity with a populated ErrorsDTO
     *
     * @see org.springframework.web.servlet.mvc.method.annotation.
     *      ResponseEntityExceptionHandler
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        logError(((ServletWebRequest) request).getRequest(), ex);
        return new ResponseEntity<>(body, headers, status);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return handleExceptionInternal(ex, ex.getMessage(), headers, status, request);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleAuthenticationException(NotFoundException ex, HttpServletRequest httpRequest) {
        logBadRequest(httpRequest, ex);
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception exception, HttpServletRequest httpRequest) {
        logError(httpRequest, getCause(exception));
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    private static void logBadRequest(HttpServletRequest httpRequest, Throwable throwable) {
        String restRequest = buildRequestString(httpRequest);
        logger.info(restRequest, throwable);
    }

    private static void logWarn(HttpServletRequest httpRequest, Throwable throwable) {
        String restRequest = buildRequestString(httpRequest);
        logger.warn(restRequest, throwable);
    }

    private static void logError(HttpServletRequest httpRequest, Throwable throwable) {
        String restRequest = buildRequestString(httpRequest);
        logger.error(restRequest, throwable);
    }

    private static String buildRequestString(HttpServletRequest httpRequest) {
        StringBuilder errorMessageBuilder = new StringBuilder(httpRequest.getRemoteAddr());
        errorMessageBuilder.append(" ");
        errorMessageBuilder.append(httpRequest.getMethod());
        errorMessageBuilder.append(" ");
        errorMessageBuilder.append(httpRequest.getRequestURI());
        return errorMessageBuilder.toString();
    }

    private static Throwable getCause(Throwable throwable) {
        Throwable cause = throwable.getCause();
        if (cause == null) {
            return throwable;
        }
        return cause;
    }
}
