package com.pagamento.pag_facil.dto;

import javax.xml.crypto.Data;

public record AuthorizationDTO(
        Data data,
        String status

) {
    public boolean isAuthorization() {
        return data != null && data.authorization;
    }

    public record Data(boolean authorization){

    }
}
