package com.proarte.erp.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.sql.SQLException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== Custom Application Exceptions ====================

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ErrorCode.ERR_NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        log.warn("Error de negocio [{}]: {}", ex.getErrorCode(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(UnauthorizedException ex) {
        log.warn("Acceso no autorizado: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ErrorCode.ERR_AUTH, ex.getMessage()));
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiResponse<Void>> handleInsufficientStock(InsufficientStockException ex) {
        log.warn("Stock insuficiente: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ErrorCode.ERR_STOCK, ex.getMessage()));
    }

    // ==================== Spring Security Exceptions ====================

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Acceso denegado: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ErrorCode.ERR_FORBIDDEN));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Credenciales invalidas");
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ErrorCode.ERR_AUTH));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Error de autenticacion: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ErrorCode.ERR_AUTH, "Error de autenticacion"));
    }

    // ==================== Spring Validation Exceptions ====================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationErrors(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(this::buildFieldErrorMessage)
                .collect(Collectors.joining("; "));

        log.warn("Error de validacion: {}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.ERR_VALIDATION, message));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("El parametro '%s' tiene un formato invalido", ex.getName());
        log.warn("Error de tipo de argumento: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.ERR_VALIDATION, message));
    }

    // ==================== Database Exceptions ====================

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Violacion de integridad de datos: {}", ex.getMostSpecificCause().getMessage());

        String message = mapDataIntegrityMessage(ex);
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ErrorCode.ERR_CONFLICT, message));
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ApiResponse<Void>> handleSqlException(SQLException ex) {
        log.error("Error SQL [{}]: {}", ex.getSQLState(), ex.getMessage());
        return mapPostgresException(ex);
    }

    // ==================== Generic / Fallback ====================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Error no manejado: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.ERR_INTERNAL));
    }

    // ==================== Helper Methods ====================

    private String buildFieldErrorMessage(FieldError fieldError) {
        String field = fieldError.getField();
        String defaultMessage = fieldError.getDefaultMessage();
        return String.format("El campo '%s' %s", field, defaultMessage != null ? defaultMessage : "es invalido");
    }

    /**
     * Maps PostgreSQL RAISE EXCEPTION SQL states to appropriate HTTP responses.
     * PostgreSQL custom error states:
     * - P0001: raise_exception (generic business error from RAISE EXCEPTION)
     * - 23505: unique_violation
     * - 23503: foreign_key_violation
     * - 23502: not_null_violation
     * - 23514: check_violation
     */
    private ResponseEntity<ApiResponse<Void>> mapPostgresException(SQLException ex) {
        String sqlState = ex.getSQLState();
        String dbMessage = ex.getMessage();

        return switch (sqlState) {
            // RAISE EXCEPTION from PL/pgSQL functions (business logic errors)
            case "P0001" -> {
                ErrorCode code = extractErrorCodeFromMessage(dbMessage);
                String userMessage = extractUserMessage(dbMessage);
                yield ResponseEntity
                        .status(mapErrorCodeToHttpStatus(code))
                        .body(ApiResponse.error(code, userMessage));
            }
            // unique_violation
            case "23505" -> ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(ErrorCode.ERR_CONFLICT, "Ya existe un registro con estos datos"));
            // foreign_key_violation
            case "23503" -> ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(ErrorCode.ERR_CONFLICT,
                            "No se puede realizar la operacion porque existen registros relacionados"));
            // not_null_violation
            case "23502" -> ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ErrorCode.ERR_VALIDATION, "Faltan campos obligatorios"));
            // check_violation
            case "23514" -> ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ErrorCode.ERR_VALIDATION, "Los datos no cumplen las restricciones requeridas"));
            // Default database error
            default -> ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(ErrorCode.ERR_INTERNAL));
        };
    }

    /**
     * Extracts an ErrorCode from a PostgreSQL RAISE EXCEPTION message.
     * Convention: messages from procedures may start with the error code prefix,
     * e.g., "ERR_STOCK: No hay suficiente stock de 'Cables XLR'"
     */
    private ErrorCode extractErrorCodeFromMessage(String message) {
        if (message == null) {
            return ErrorCode.ERR_BUSINESS;
        }
        for (ErrorCode code : ErrorCode.values()) {
            if (message.toUpperCase().contains(code.getCode())) {
                return code;
            }
        }
        return ErrorCode.ERR_BUSINESS;
    }

    /**
     * Extracts the user-facing message from a PostgreSQL RAISE EXCEPTION.
     * Strips the error code prefix if present.
     */
    private String extractUserMessage(String message) {
        if (message == null) {
            return ErrorCode.ERR_BUSINESS.getDefaultMessage();
        }
        // If message follows pattern "ERR_CODE: actual message", extract the message part
        for (ErrorCode code : ErrorCode.values()) {
            String prefix = code.getCode() + ":";
            if (message.toUpperCase().contains(prefix)) {
                int idx = message.toUpperCase().indexOf(prefix);
                return message.substring(idx + prefix.length()).trim();
            }
        }
        // Otherwise return the raw message (already in Spanish from procedure)
        return message;
    }

    /**
     * Maps our application ErrorCode to the appropriate HTTP status.
     */
    private HttpStatus mapErrorCodeToHttpStatus(ErrorCode code) {
        return switch (code) {
            case ERR_AUTH -> HttpStatus.UNAUTHORIZED;
            case ERR_FORBIDDEN -> HttpStatus.FORBIDDEN;
            case ERR_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case ERR_VALIDATION -> HttpStatus.BAD_REQUEST;
            case ERR_BUSINESS -> HttpStatus.UNPROCESSABLE_ENTITY;
            case ERR_STOCK -> HttpStatus.CONFLICT;
            case ERR_CONFLICT -> HttpStatus.CONFLICT;
            case ERR_INTERNAL -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    /**
     * Maps DataIntegrityViolationException to a user-friendly Spanish message.
     */
    private String mapDataIntegrityMessage(DataIntegrityViolationException ex) {
        String rootCause = ex.getMostSpecificCause().getMessage();
        if (rootCause != null) {
            if (rootCause.contains("unique") || rootCause.contains("duplicate")) {
                return "Ya existe un registro con estos datos";
            }
            if (rootCause.contains("foreign key") || rootCause.contains("fk_")) {
                return "No se puede realizar la operacion porque existen registros relacionados";
            }
            if (rootCause.contains("not-null") || rootCause.contains("null value")) {
                return "Faltan campos obligatorios";
            }
        }
        return ErrorCode.ERR_CONFLICT.getDefaultMessage();
    }
}
