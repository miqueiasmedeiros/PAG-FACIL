package com.pagamento.pag_facil.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferNotificationMessage(
        Long transactionId,
        BigDecimal value,
        Long senderId,
        Long receiverId,
        LocalDateTime createdAt
) {
}

