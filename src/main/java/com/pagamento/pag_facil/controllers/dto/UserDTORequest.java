package com.pagamento.pag_facil.controllers.dto;


import com.pagamento.pag_facil.domain.user.UserType;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
public class UserDTORequest {

    public Long id;
    public String firstName;
    private BigDecimal balance;
    private String lastName;
    private String document;
    private String email;
    private String password;
    private UserType userType;

}
