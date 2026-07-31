package com.proarte.erp.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProcedureRequest(
        Map<String, Object> params
) {
}
