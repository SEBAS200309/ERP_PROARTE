package com.proarte.erp.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    // ==================== Custom Exceptions ====================

    @Test
    @DisplayName("ResourceNotFoundException retorna 404 con ERR_NOT_FOUND")
    void handleResourceNotFound() {
        var ex = new ResourceNotFoundException("Usuario", "id", "abc-123");

        ResponseEntity<ApiResponse<Void>> response = handler.handleResourceNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().error().code()).isEqualTo("ERR_NOT_FOUND");
        assertThat(response.getBody().error().message()).contains("Usuario");
    }

    @Test
    @DisplayName("BusinessException retorna 422 con ERR_BUSINESS")
    void handleBusinessException() {
        var ex = new BusinessException("La cotizacion no esta aprobada");

        ResponseEntity<ApiResponse<Void>> response = handler.handleBusinessException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().error().code()).isEqualTo("ERR_BUSINESS");
        assertThat(response.getBody().error().message()).isEqualTo("La cotizacion no esta aprobada");
    }

    @Test
    @DisplayName("BusinessException con codigo custom usa el codigo proporcionado")
    void handleBusinessExceptionWithCustomCode() {
        var ex = new BusinessException("Error personalizado", "ERR_CUSTOM");

        ResponseEntity<ApiResponse<Void>> response = handler.handleBusinessException(ex);

        assertThat(response.getBody().error().code()).isEqualTo("ERR_CUSTOM");
    }

    @Test
    @DisplayName("UnauthorizedException retorna 401 con ERR_AUTH")
    void handleUnauthorized() {
        var ex = new UnauthorizedException();

        ResponseEntity<ApiResponse<Void>> response = handler.handleUnauthorized(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("ERR_AUTH");
        assertThat(response.getBody().error().message()).isEqualTo("Credenciales incorrectas");
    }

    @Test
    @DisplayName("InsufficientStockException retorna 409 con ERR_STOCK")
    void handleInsufficientStock() {
        var ex = new InsufficientStockException("Cables XLR", 10, 3);

        ResponseEntity<ApiResponse<Void>> response = handler.handleInsufficientStock(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("ERR_STOCK");
        assertThat(response.getBody().error().message()).contains("Cables XLR");
    }

    // ==================== Spring Security Exceptions ====================

    @Test
    @DisplayName("AccessDeniedException retorna 403 con ERR_FORBIDDEN")
    void handleAccessDenied() {
        var ex = new AccessDeniedException("Access denied");

        ResponseEntity<ApiResponse<Void>> response = handler.handleAccessDenied(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("ERR_FORBIDDEN");
        assertThat(response.getBody().error().message()).isEqualTo("No tiene permisos para esta accion");
    }

    @Test
    @DisplayName("BadCredentialsException retorna 401 con ERR_AUTH")
    void handleBadCredentials() {
        var ex = new BadCredentialsException("Bad credentials");

        ResponseEntity<ApiResponse<Void>> response = handler.handleBadCredentials(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("ERR_AUTH");
    }

    // ==================== Validation Exceptions ====================

    @Test
    @DisplayName("MethodArgumentNotValidException retorna 400 con detalles de campo")
    void handleValidationErrors() {
        var bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "email", "no debe estar vacio"));
        bindingResult.addError(new FieldError("request", "nombre", "no debe estar vacio"));
        var ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ApiResponse<Void>> response = handler.handleValidationErrors(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("ERR_VALIDATION");
        assertThat(response.getBody().error().message()).contains("email");
        assertThat(response.getBody().error().message()).contains("nombre");
    }

    @Test
    @DisplayName("MethodArgumentTypeMismatchException retorna 400")
    void handleTypeMismatch() {
        var ex = new MethodArgumentTypeMismatchException(
                "abc", Integer.class, "page", null, new NumberFormatException("abc"));

        ResponseEntity<ApiResponse<Void>> response = handler.handleTypeMismatch(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("ERR_VALIDATION");
        assertThat(response.getBody().error().message()).contains("page");
    }

    // ==================== Database Exceptions ====================

    @Test
    @DisplayName("DataIntegrityViolationException con unique violation retorna 409")
    void handleDataIntegrityUniqueViolation() {
        var rootCause = new RuntimeException("duplicate key value violates unique constraint");
        var ex = new DataIntegrityViolationException("Could not execute statement", rootCause);

        ResponseEntity<ApiResponse<Void>> response = handler.handleDataIntegrity(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("ERR_CONFLICT");
        assertThat(response.getBody().error().message()).contains("Ya existe un registro");
    }

    @Test
    @DisplayName("SQLException con P0001 (RAISE EXCEPTION) mapea error de negocio")
    void handleSqlExceptionRaiseException() {
        var ex = new SQLException("ERR_STOCK: No hay suficiente stock de 'Cables XLR'", "P0001");

        ResponseEntity<ApiResponse<Void>> response = handler.handleSqlException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("ERR_STOCK");
        assertThat(response.getBody().error().message()).contains("Cables XLR");
    }

    @Test
    @DisplayName("SQLException con P0001 sin prefijo conocido usa ERR_BUSINESS")
    void handleSqlExceptionGenericRaise() {
        var ex = new SQLException("La cotizacion debe estar aprobada", "P0001");

        ResponseEntity<ApiResponse<Void>> response = handler.handleSqlException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("ERR_BUSINESS");
        assertThat(response.getBody().error().message()).isEqualTo("La cotizacion debe estar aprobada");
    }

    @Test
    @DisplayName("SQLException con 23505 (unique_violation) retorna 409")
    void handleSqlExceptionUniqueViolation() {
        var ex = new SQLException("duplicate key value", "23505");

        ResponseEntity<ApiResponse<Void>> response = handler.handleSqlException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().error().code()).isEqualTo("ERR_CONFLICT");
    }

    @Test
    @DisplayName("SQLException con 23503 (foreign_key_violation) retorna 409")
    void handleSqlExceptionForeignKeyViolation() {
        var ex = new SQLException("violates foreign key constraint", "23503");

        ResponseEntity<ApiResponse<Void>> response = handler.handleSqlException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().error().code()).isEqualTo("ERR_CONFLICT");
    }

    // ==================== Generic Exception ====================

    @Test
    @DisplayName("Exception generica retorna 500 con ERR_INTERNAL")
    void handleGenericException() {
        var ex = new RuntimeException("Unexpected error");

        ResponseEntity<ApiResponse<Void>> response = handler.handleGenericException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().error().code()).isEqualTo("ERR_INTERNAL");
        assertThat(response.getBody().error().message()).isEqualTo("Ocurrio un error interno. Intente mas tarde");
    }

    // ==================== Response Format Consistency ====================

    @Test
    @DisplayName("Todas las respuestas de error tienen success=false y data=null")
    void errorResponsesHaveConsistentFormat() {
        var ex = new ResourceNotFoundException("Test");

        ResponseEntity<ApiResponse<Void>> response = handler.handleResourceNotFound(ex);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().data()).isNull();
        assertThat(response.getBody().error()).isNotNull();
        assertThat(response.getBody().error().code()).isNotBlank();
        assertThat(response.getBody().error().message()).isNotBlank();
    }
}
