package com.pagamento.pag_facil.repo;

import com.pagamento.pag_facil.domain.transaction.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.ListCrudRepository;

public interface ITransactionRepo extends JpaRepository<Transaction, Long> {

}
