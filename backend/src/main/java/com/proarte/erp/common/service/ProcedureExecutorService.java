package com.proarte.erp.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proarte.erp.exception.BusinessException;
import com.proarte.erp.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcedureExecutorService {

    private static final Pattern FUNCTION_NAME_PATTERN = Pattern.compile("^[a-z_][a-z0-9_]{0,63}$");

    /**
     * Whitelist de funciones PostgreSQL autorizadas para ejecucion via API.
     * Solo las funciones registradas aqui pueden ser invocadas desde el endpoint generico.
     * Para agregar una nueva funcion, registrarla en este Set.
     */
    private static final Set<String> ALLOWED_FUNCTIONS = Set.of(
            "fn_recalcular_total_cotizacion",
            "fn_crear_evento_desde_cotizacion",
            "fn_calcular_valor_turno"
    );

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Ejecuta una funcion PostgreSQL pasando los parametros como JSONB.
     *
     * @param functionName nombre de la funcion (validado contra whitelist)
     * @param params       parametros a enviar como JSON
     * @return resultado de la funcion parseado como Map
     */
    @Transactional
    public Map<String, Object> executeFunction(String functionName, Map<String, Object> params) {
        validateFunctionName(functionName);
        validateFunctionAllowed(functionName);

        String paramsJson = serializeParams(params);

        log.info("Ejecutando funcion: {}() con parametros: {}", functionName, paramsJson);
        long startTime = System.currentTimeMillis();

        try {
            String sql = "SELECT " + functionName + "(?::jsonb)";
            String result = jdbcTemplate.queryForObject(sql, String.class, paramsJson);

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("Funcion {}() ejecutada exitosamente en {} ms", functionName, elapsed);

            return parseResult(result);
        } catch (org.springframework.dao.DataAccessException ex) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("Error ejecutando funcion {}() despues de {} ms: {}", functionName, elapsed, ex.getMessage());
            throw mapDatabaseException(ex);
        }
    }

    /**
     * Verifica si una funcion esta en la whitelist de funciones permitidas.
     */
    public boolean isFunctionAllowed(String functionName) {
        return functionName != null && ALLOWED_FUNCTIONS.contains(functionName);
    }

    private void validateFunctionName(String functionName) {
        if (functionName == null || !FUNCTION_NAME_PATTERN.matcher(functionName).matches()) {
            throw new BusinessException(
                    "Nombre de funcion invalido: solo se permiten letras minusculas, numeros y guion bajo",
                    "ERR_VALIDATION"
            );
        }
    }

    private void validateFunctionAllowed(String functionName) {
        if (!ALLOWED_FUNCTIONS.contains(functionName)) {
            log.warn("Intento de ejecucion de funcion no autorizada: {}", functionName);
            throw new UnauthorizedException(
                    "La funcion '" + functionName + "' no esta autorizada para ejecucion via API"
            );
        }
    }

    private String serializeParams(Map<String, Object> params) {
        try {
            Map<String, Object> safeParams = (params != null) ? params : Collections.emptyMap();
            return objectMapper.writeValueAsString(safeParams);
        } catch (JsonProcessingException ex) {
            log.error("Error serializando parametros: {}", ex.getMessage());
            throw new BusinessException("Error al procesar los parametros de entrada", "ERR_VALIDATION");
        }
    }

    private Map<String, Object> parseResult(String jsonResult) {
        if (jsonResult == null || jsonResult.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(jsonResult, new TypeReference<>() {});
        } catch (JsonProcessingException ex) {
            log.warn("Resultado de la funcion no es un JSON valido, retornando como valor simple: {}", ex.getMessage());
            return Map.of("result", jsonResult);
        }
    }

    private BusinessException mapDatabaseException(org.springframework.dao.DataAccessException ex) {
        String message = ex.getMostSpecificCause().getMessage();

        if (message != null && message.contains("ERROR:")) {
            String cleanMessage = message.substring(message.indexOf("ERROR:") + 7).trim();
            int newlineIdx = cleanMessage.indexOf('\n');
            if (newlineIdx > 0) {
                cleanMessage = cleanMessage.substring(0, newlineIdx).trim();
            }
            return new BusinessException(cleanMessage, "ERR_BUSINESS");
        }

        return new BusinessException(
                "Error al ejecutar la operacion en base de datos",
                "ERR_BUSINESS"
        );
    }
}
