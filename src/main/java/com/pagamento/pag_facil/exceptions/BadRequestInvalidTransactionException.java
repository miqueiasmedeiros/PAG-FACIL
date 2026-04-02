package com.pagamento.pag_facil.exceptions;

public class BadRequestInvalidTransactionException extends RuntimeException {
    public BadRequestInvalidTransactionException(String message) {
        super(message);
    }
}
