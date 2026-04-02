package com.pagamento.pag_facil.integration.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pagamento.pag_facil.domain.outbox.OutboxMessage;
import com.pagamento.pag_facil.domain.outbox.OutboxStatus;
import com.pagamento.pag_facil.dto.TransferNotificationMessage;
import com.pagamento.pag_facil.integration.rabbit.NotificationProducer;
import com.pagamento.pag_facil.repo.OutboxMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherTest {

    @Mock
    private OutboxMessageRepository repository;

    @Mock
    private NotificationProducer notificationProducer;

    private OutboxPublisher publisher;

    private ObjectMapper mapper;

    @BeforeEach
    void setup() {
        mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        publisher = new OutboxPublisher(repository, notificationProducer, mapper);
    }

    @Test
    void shouldMarkMessageSentWhenPublishSucceeds() throws Exception {
        OutboxMessage message = pendingMessage();
        when(repository.findTop50ByStatusInAndAvailableAtBeforeOrderByCreatedAtAsc(anyList(), any()))
                .thenReturn(List.of(message));

        publisher.publishPendingMessages();

        verify(notificationProducer).sendNotification(any(TransferNotificationMessage.class));

        ArgumentCaptor<List<OutboxMessage>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(captor.capture());
        OutboxMessage saved = captor.getValue().getFirst();
        assertThat(saved.getStatus()).isEqualTo(OutboxStatus.SENT);
        assertThat(saved.getLastError()).isNull();
    }

    @Test
    void shouldRetryAndStayPendingWhenPublishFails() throws Exception {
        OutboxMessage message = pendingMessage();
        when(repository.findTop50ByStatusInAndAvailableAtBeforeOrderByCreatedAtAsc(anyList(), any()))
                .thenReturn(List.of(message));
        doThrow(new IllegalStateException("fail"))
                .when(notificationProducer).sendNotification(any(TransferNotificationMessage.class));

        publisher.publishPendingMessages();

        ArgumentCaptor<List<OutboxMessage>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(captor.capture());
        OutboxMessage saved = captor.getValue().getFirst();
        assertThat(saved.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(saved.getRetryCount()).isEqualTo(1);
        assertThat(saved.getLastError()).contains("fail");
        assertThat(saved.getAvailableAt()).isAfter(LocalDateTime.now().minusSeconds(1));
    }

    private OutboxMessage pendingMessage() throws Exception {
        TransferNotificationMessage payload = new TransferNotificationMessage(1L, new BigDecimal("10.00"), 2L, 3L, LocalDateTime.now());
        OutboxMessage message = new OutboxMessage();
        message.setId(99L);
        message.setStatus(OutboxStatus.PENDING);
        message.setRetryCount(0);
        message.setPayload(mapper.writeValueAsString(payload));
        message.setCreatedAt(LocalDateTime.now().minusMinutes(1));
        message.setAvailableAt(LocalDateTime.now().minusSeconds(5));
        return message;
    }
}

