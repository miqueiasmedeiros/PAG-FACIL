package com.pagamento.pag_facil.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pagamento.pag_facil.domain.outbox.OutboxMessage;
import com.pagamento.pag_facil.domain.outbox.OutboxStatus;
import com.pagamento.pag_facil.domain.transaction.Transaction;
import com.pagamento.pag_facil.domain.user.User;
import com.pagamento.pag_facil.dto.TransferNotificationMessage;
import com.pagamento.pag_facil.repo.OutboxMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OutboxServiceTest {

    @Mock
    private OutboxMessageRepository outboxMessageRepository;

    private OutboxService outboxService;

    private ObjectMapper mapper;

    @BeforeEach
    void setupMapper() {
        mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        outboxService = new OutboxService(outboxMessageRepository, mapper);
    }

    @Test
    void shouldPersistPendingOutboxWithSerializedPayload() {
        Transaction tx = new Transaction();
        tx.setId(10L);
        tx.setAmount(new BigDecimal("50.00"));
        tx.setTimestamp(LocalDateTime.now());

        User sender = new User();
        sender.setId(1L);
        tx.setSender(sender);

        User receiver = new User();
        receiver.setId(2L);
        tx.setReceiver(receiver);

        outboxService.saveTransferNotification(tx);

        ArgumentCaptor<OutboxMessage> captor = ArgumentCaptor.forClass(OutboxMessage.class);
        verify(outboxMessageRepository).save(captor.capture());

        OutboxMessage saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(saved.getRetryCount()).isZero();
        assertThat(saved.getEventType()).isEqualTo("TRANSFER_COMPLETED");
        assertThat(saved.getPayload()).isNotBlank();

        TransferNotificationMessage parsed = parse(saved.getPayload());
        assertThat(parsed.transactionId()).isEqualTo(10L);
        assertThat(parsed.value()).isEqualByComparingTo("50.00");
        assertThat(parsed.senderId()).isEqualTo(1L);
        assertThat(parsed.receiverId()).isEqualTo(2L);
    }

    private TransferNotificationMessage parse(String json) {
        try {
            return mapper.readValue(json, TransferNotificationMessage.class);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}


