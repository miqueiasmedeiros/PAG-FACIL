package com.pagamento.pag_facil.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pagamento.pag_facil.domain.outbox.OutboxMessage;
import com.pagamento.pag_facil.domain.outbox.OutboxStatus;
import com.pagamento.pag_facil.domain.transaction.Transaction;
import com.pagamento.pag_facil.dto.TransferNotificationMessage;
import com.pagamento.pag_facil.repo.OutboxMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {

    private static final String EVENT_TRANSFER_COMPLETED = "TRANSFER_COMPLETED";

    private final OutboxMessageRepository outboxMessageRepository;
    private final ObjectMapper objectMapper;

    public void saveTransferNotification(Transaction transaction) {
        TransferNotificationMessage message = new TransferNotificationMessage(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getSender().getId(),
                transaction.getReceiver().getId(),
                transaction.getTimestamp()
        );

        try {
            OutboxMessage outboxMessage = OutboxMessage.builder()
                    .eventType(EVENT_TRANSFER_COMPLETED)
                    .status(OutboxStatus.PENDING)
                    .payload(objectMapper.writeValueAsString(message))
                    .retryCount(0)
                    .availableAt(LocalDateTime.now())
                    .build();

            outboxMessageRepository.save(outboxMessage);
        } catch (JsonProcessingException ex) {
            log.error("Failed to serialize notification message", ex);
            throw new IllegalStateException("Unable to serialize transfer notification", ex);
        }
    }
}

