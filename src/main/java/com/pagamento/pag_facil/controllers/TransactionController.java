package com.pagamento.pag_facil.controllers;

import com.pagamento.pag_facil.dto.TransactionDTORequest;
import com.pagamento.pag_facil.dto.TransactionDTOResponse;
import com.pagamento.pag_facil.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

//    @PostMapping
//    public ResponseEntity<Transaction> createTransaction(@RequestBody TransactionDTORequest transactionDTO){
//        Transaction newTransaction = this.transactionService.createTransaction(transactionDTO);
//        return new ResponseEntity<>(newTransaction, HttpStatus.CREATED);
//    }
    @PostMapping
    public ResponseEntity<TransactionDTOResponse> createTransaction(@RequestBody TransactionDTORequest transactionDTO){
        TransactionDTOResponse newTransaction = this.transactionService.createTransaction(transactionDTO);
        return new ResponseEntity<>(newTransaction, HttpStatus.CREATED);
    }

}
