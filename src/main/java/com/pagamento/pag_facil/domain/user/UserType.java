package com.pagamento.pag_facil.domain.user;

public enum UserType {
    COMUM(1),
    LOGISTA(2);

    private int value;

    UserType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
