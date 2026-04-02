package com.pagamento.pag_facil.integration.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pagamento.pag_facil.domain.outbox.OutboxMessage;
import com.pagamento.pag_facil.domain.outbox.OutboxStatus;
import com.pagamento.pag_facil.dto.TransferNotificationMessage;
import com.pagamento.pag_facil.integration.rabbit.NotificationProducer;
import com.pagamento.pag_facil.repo.OutboxMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private static final int MAX_RETRIES = 3;

    private final OutboxMessageRepository outboxMessageRepository;
    private final NotificationProducer notificationProducer;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${app.outbox.dispatch-interval-ms:5000}")
    public void publishPendingMessages() {
        List<OutboxMessage> pendingMessages = outboxMessageRepository
                .findTop50ByStatusInAndAvailableAtBeforeOrderByCreatedAtAsc(
                        List.of(OutboxStatus.PENDING), LocalDateTime.now());

        if (pendingMessages.isEmpty()) {
            return;
        }

        pendingMessages.forEach(this::tryPublish);
        outboxMessageRepository.saveAll(pendingMessages);
    }

    private void tryPublish(OutboxMessage outboxMessage) {
        try {
            TransferNotificationMessage payload = objectMapper.readValue(
                    outboxMessage.getPayload(), TransferNotificationMessage.class);

            notificationProducer.sendNotification(payload);
            outboxMessage.setStatus(OutboxStatus.SENT);
            outboxMessage.setLastError(null);
        } catch (Exception ex) {
            int attempts = outboxMessage.getRetryCount() + 1;
            outboxMessage.setRetryCount(attempts);
            outboxMessage.setLastError(ex.getMessage());

            if (attempts >= MAX_RETRIES) {
                outboxMessage.setStatus(OutboxStatus.FAILED);
                log.error("Outbox message {} moved to FAILED after {} attempts", outboxMessage.getId(), attempts, ex);
                return;
            }

            outboxMessage.setStatus(OutboxStatus.PENDING);
            outboxMessage.setAvailableAt(LocalDateTime.now().plusSeconds(backoffSeconds(attempts)));
            log.warn("Outbox message {} will retry. Attempt {}", outboxMessage.getId(), attempts, ex);
        }
    }

    private long backoffSeconds(int attempts) {
        long delay = attempts * 5L;
        return Math.min(delay, 60L);
    }
}

