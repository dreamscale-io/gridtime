package com.dreamscale.htmflow.core.domain;

public enum ActiveAccountStatus {

    //Note, these are mapped to DB enum entries, so order is fixed in DB history
    Online(1), Offline(2);

    private final int order;

    ActiveAccountStatus(int order) {
        this.order = order;
    }

    public int getOrder() {
        return this.order;
    }
}
