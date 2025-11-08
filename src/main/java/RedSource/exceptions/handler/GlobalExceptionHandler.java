package RedSource.exceptions.handler;

import RedSource.entities.utils.ResponseUtils;
import RedSource.exceptions.ResourceNotFoundException;
import RedSource.exceptions.ServiceException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFoundException(ResourceNotFoundException e, HttpServletRequest request) {
        logger.warn("Resource not found: {} - Path: {} {}", 
                e.getMessage(), 
                request.getMethod(), 
                request.getRequestURI());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ResponseUtils.buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage()));
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<?> handleServiceException(ServiceException e, HttpServletRequest request) {
        // Check if the message indicates a resource not found scenario
        String message = e.getMessage().toLowerCase();
        if (message.contains("not found") || message.contains("does not exist")) {
            logger.warn("Service exception (not found): {} - Path: {} {}", 
                    e.getMessage(), 
                    request.getMethod(), 
                    request.getRequestURI());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ResponseUtils.buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage()));
        }
        // Default to internal server error for other service exceptions
        logger.error("Service exception: {} - Path: {} {} - Stack trace: ", 
                e.getMessage(), 
                request.getMethod(), 
                request.getRequestURI(), 
                e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseUtils.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentialsException(BadCredentialsException e, HttpServletRequest request) {
        logger.warn("Authentication failed - Invalid credentials - Path: {} {} - IP: {}", 
                request.getMethod(), 
                request.getRequestURI(),
                getClientIpAddress(request));
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ResponseUtils.buildErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        logger.warn("Access denied: {} - Path: {} {} - IP: {}", 
                e.getMessage(), 
                request.getMethod(), 
                request.getRequestURI(),
                getClientIpAddress(request));
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ResponseUtils.buildErrorResponse(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage()));
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<?> handleInsufficientAuthenticationException(InsufficientAuthenticationException e, HttpServletRequest request) {
        logger.warn("Authentication required - Path: {} {} - IP: {}", 
                request.getMethod(), 
                request.getRequestURI(),
                getClientIpAddress(request));
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ResponseUtils.buildErrorResponse(HttpStatus.UNAUTHORIZED, "Authentication required"));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<?> handleValidationException(Exception e, HttpServletRequest request) {
        logger.warn("Validation error: {} - Path: {} {}", 
                e.getMessage(), 
                request.getMethod(), 
                request.getRequestURI());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResponseUtils.buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation error: " + e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        logger.warn("Illegal argument: {} - Path: {} {}", 
                e.getMessage(), 
                request.getMethod(), 
                request.getRequestURI());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResponseUtils.buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e, HttpServletRequest request) {
        logger.error("Unexpected error: {} - Path: {} {} - IP: {} - Stack trace: ", 
                e.getMessage(), 
                request.getMethod(), 
                request.getRequestURI(),
                getClientIpAddress(request),
                e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseUtils.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred: " + e.getMessage()));
    }

    /**
     * Helper method to get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
