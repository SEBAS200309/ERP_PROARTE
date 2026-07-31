package com.proarte.erp.exception;

import lombok.Getter;

@Getter
public class InsufficientStockException extends RuntimeException {

    private final String itemName;
    private final Number requestedQuantity;
    private final Number availableQuantity;

    public InsufficientStockException(String itemName, Number requestedQuantity, Number availableQuantity) {
        super(String.format("No hay suficiente stock de '%s'. Solicitado: %s, Disponible: %s",
                itemName, requestedQuantity, availableQuantity));
        this.itemName = itemName;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    public InsufficientStockException(String message) {
        super(message);
        this.itemName = null;
        this.requestedQuantity = null;
        this.availableQuantity = null;
    }
}
