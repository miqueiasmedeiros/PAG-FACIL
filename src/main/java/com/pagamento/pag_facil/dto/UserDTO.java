package com.pagamento.pag_facil.dto;

import com.pagamento.pag_facil.domain.user.UserType;

import java.math.BigDecimal;

public record UserDTO(
       Long id,  String firstName, String lastName, String document, BigDecimal balance, UserType userType) {
}
