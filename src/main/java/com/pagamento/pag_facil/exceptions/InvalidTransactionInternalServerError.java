package com.pagamento.pag_facil.exceptions;

public class InvalidTransactionInternalServerError extends RuntimeException {
    public InvalidTransactionInternalServerError(String message) {
        super(message);
    }
}
