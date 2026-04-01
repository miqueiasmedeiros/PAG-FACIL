package com.pagamento.pag_facil.dto;

import com.pagamento.pag_facil.domain.user.UserType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionDTOResponse(
        Long id,
        BigDecimal amount,
        ParticipantSummary sender,
        ParticipantSummary receiver,
        LocalDateTime timestamp) {

    public record ParticipantSummary(
            Long id,
            String firstName,
            BigDecimal balance,
            UserType userType) {
    }
}

