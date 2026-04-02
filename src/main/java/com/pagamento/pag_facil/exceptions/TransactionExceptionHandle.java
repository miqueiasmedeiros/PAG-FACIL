package com.pagamento.pag_facil.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.Map;

@ControllerAdvice
public class TransactionExceptionHandle {

    private Map<String, Object> buildBody(HttpStatus status, String message) {
        return Map.of(
                "timestamp", LocalDateTime.now(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message
        );
    }

    @ExceptionHandler(BadRequestInvalidTransactionException.class)
    public ResponseEntity<Map<String, Object>>handleInvalidTransaction(BadRequestInvalidTransactionException exception){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(buildBody(HttpStatus.BAD_REQUEST, exception.getMessage()));
    }
    @ExceptionHandler(InvalidTransactionInternalServerError.class)
    public ResponseEntity<Map<String, Object>>handleInvalidTransactionInternalError(InvalidTransactionInternalServerError exception){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(buildBody(HttpStatus.FORBIDDEN, exception.getMessage()));
    }
    @ExceptionHandler(UnprocessableEntity.class)
    public ResponseEntity<Map<String, Object>>handleInvalidUnprocessable(UnprocessableEntity exception){
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(buildBody(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage()));
    }
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>>handleNotFoundException(NotificationException exception){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(buildBody(HttpStatus.NOT_FOUND, exception.getMessage()));
    }


    @ExceptionHandler(UnauthorizedTransactionException.class)
    public ResponseEntity<Map<String, Object>>handleUnauthorizedTransaction(UnauthorizedTransactionException exception){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildBody(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage()));
    }
}
