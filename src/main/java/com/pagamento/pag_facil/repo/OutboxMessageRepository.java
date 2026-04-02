package com.pagamento.pag_facil.repo;

import com.pagamento.pag_facil.domain.outbox.OutboxMessage;
import com.pagamento.pag_facil.domain.outbox.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, Long> {

    List<OutboxMessage> findTop50ByStatusInAndAvailableAtBeforeOrderByCreatedAtAsc(List<OutboxStatus> statuses, LocalDateTime availableAt);
}

