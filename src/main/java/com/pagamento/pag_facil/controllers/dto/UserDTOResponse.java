package com.pagamento.pag_facil.controllers.dto;


import com.pagamento.pag_facil.domain.user.User;
import com.pagamento.pag_facil.domain.user.UserType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class UserDTOResponse {

    private Long id;
    private String firstName;
    private BigDecimal balance;
    private UserType userType;

    public UserDTOResponse(User user) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.balance = user.getBalance();
        this.userType = user.getUserType();
    }
}
