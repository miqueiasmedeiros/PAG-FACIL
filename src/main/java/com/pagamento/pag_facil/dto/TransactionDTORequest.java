package com.pagamento.pag_facil.dto;

import java.math.BigDecimal;

public record TransactionDTORequest(
        BigDecimal value,
        Long senderId,
        Long receiverId) {
}

