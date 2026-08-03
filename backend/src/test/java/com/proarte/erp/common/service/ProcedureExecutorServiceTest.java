package com.proarte.erp.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proarte.erp.exception.BusinessException;
import com.proarte.erp.exception.UnauthorizedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcedureExecutorServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ProcedureExecutorService procedureExecutorService;

    @Test
    @DisplayName("executeFunction ejecuta funcion permitida y retorna resultado parseado")
    void shouldExecuteFunction_whenFunctionIsAllowed() throws JsonProcessingException {
        String functionName = "fn_recalcular_total_cotizacion";
        Map<String, Object> params = Map.of("cotizacion_id", "uuid-123");
        String paramsJson = "{\"cotizacion_id\":\"uuid-123\"}";
        String resultJson = "{\"total\":1500.00}";

        when(objectMapper.writeValueAsString(params)).thenReturn(paramsJson);
        when(jdbcTemplate.queryForObject(
                eq("SELECT fn_recalcular_total_cotizacion(?::jsonb)"),
                eq(String.class),
                eq(paramsJson)
        )).thenReturn(resultJson);
        when(objectMapper.readValue(eq(resultJson), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(Map.of("total", 1500.00));

        Map<String, Object> result = procedureExecutorService.executeFunction(functionName, params);

        assertThat(result).containsEntry("total", 1500.00);
    }

    @Test
    @DisplayName("executeFunction lanza UnauthorizedException para funcion no en whitelist")
    void shouldThrowUnauthorized_whenFunctionNotInWhitelist() {
        assertThatThrownBy(() -> procedureExecutorService.executeFunction("fn_malicious_drop_table", Map.of()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("no esta autorizada");
    }

    @Test
    @DisplayName("executeFunction lanza BusinessException para nombre con caracteres invalidos")
    void shouldThrowBusinessException_whenFunctionNameHasInvalidChars() {
        assertThatThrownBy(() -> procedureExecutorService.executeFunction("DROP TABLE users; --", Map.of()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Nombre de funcion invalido");
    }

    @Test
    @DisplayName("executeFunction lanza BusinessException para nombre con mayusculas")
    void shouldThrowBusinessException_whenFunctionNameHasUpperCase() {
        assertThatThrownBy(() -> procedureExecutorService.executeFunction("FN_MaliciousFunction", Map.of()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Nombre de funcion invalido");
    }

    @Test
    @DisplayName("executeFunction lanza BusinessException para nombre null")
    void shouldThrowBusinessException_whenFunctionNameIsNull() {
        assertThatThrownBy(() -> procedureExecutorService.executeFunction(null, Map.of()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Nombre de funcion invalido");
    }

    @Test
    @DisplayName("executeFunction maneja parametros null serializandolos como objeto vacio")
    void shouldHandleNullParams() throws JsonProcessingException {
        String functionName = "fn_recalcular_total_cotizacion";

        when(objectMapper.writeValueAsString(Map.of())).thenReturn("{}");
        when(jdbcTemplate.queryForObject(
                eq("SELECT fn_recalcular_total_cotizacion(?::jsonb)"),
                eq(String.class),
                eq("{}")
        )).thenReturn(null);

        Map<String, Object> result = procedureExecutorService.executeFunction(functionName, null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("executeFunction lanza BusinessException cuando JdbcTemplate falla con DataAccessException")
    void shouldThrowBusinessException_whenDatabaseFails() throws JsonProcessingException {
        String functionName = "fn_recalcular_total_cotizacion";

        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(jdbcTemplate.queryForObject(any(String.class), eq(String.class), any()))
                .thenThrow(new DataAccessException("ERROR: some db error") {
                    @Override
                    public Throwable getMostSpecificCause() {
                        return new RuntimeException("ERROR: La cotizacion no existe\nHINT: check id");
                    }
                });

        assertThatThrownBy(() -> procedureExecutorService.executeFunction(functionName, Map.of()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("La cotizacion no existe");
    }

    @Test
    @DisplayName("isFunctionAllowed retorna true para funciones en whitelist")
    void shouldReturnTrue_whenFunctionIsInWhitelist() {
        assertThat(procedureExecutorService.isFunctionAllowed("fn_recalcular_total_cotizacion")).isTrue();
        assertThat(procedureExecutorService.isFunctionAllowed("fn_crear_evento_desde_cotizacion")).isTrue();
        assertThat(procedureExecutorService.isFunctionAllowed("fn_calcular_valor_turno")).isTrue();
    }

    @Test
    @DisplayName("isFunctionAllowed retorna false para funciones fuera de whitelist")
    void shouldReturnFalse_whenFunctionNotInWhitelist() {
        assertThat(procedureExecutorService.isFunctionAllowed("fn_drop_database")).isFalse();
        assertThat(procedureExecutorService.isFunctionAllowed("fn_custom_function")).isFalse();
    }

    @Test
    @DisplayName("isFunctionAllowed retorna false para null")
    void shouldReturnFalse_whenFunctionNameIsNull() {
        assertThat(procedureExecutorService.isFunctionAllowed(null)).isFalse();
    }

    @Test
    @DisplayName("executeFunction previene inyeccion SQL via nombre de funcion con punto y coma")
    void shouldPreventSqlInjection_withSemicolon() {
        assertThatThrownBy(() -> procedureExecutorService.executeFunction("fn_test; DROP TABLE users", Map.of()))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("executeFunction previene inyeccion SQL via nombre con parentesis")
    void shouldPreventSqlInjection_withParentheses() {
        assertThatThrownBy(() -> procedureExecutorService.executeFunction("fn_test()", Map.of()))
                .isInstanceOf(BusinessException.class);
    }
}
