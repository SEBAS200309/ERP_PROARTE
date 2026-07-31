package com.proarte.erp.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    ERR_AUTH("ERR_AUTH", "Credenciales incorrectas"),
    ERR_FORBIDDEN("ERR_FORBIDDEN", "No tiene permisos para esta accion"),
    ERR_NOT_FOUND("ERR_NOT_FOUND", "El recurso solicitado no fue encontrado"),
    ERR_VALIDATION("ERR_VALIDATION", "Error de validacion"),
    ERR_BUSINESS("ERR_BUSINESS", "Error de logica de negocio"),
    ERR_STOCK("ERR_STOCK", "No hay suficiente stock para este retiro"),
    ERR_CONFLICT("ERR_CONFLICT", "Existe un conflicto con los datos actuales"),
    ERR_INTERNAL("ERR_INTERNAL", "Ocurrio un error interno. Intente mas tarde");

    private final String code;
    private final String defaultMessage;
}
