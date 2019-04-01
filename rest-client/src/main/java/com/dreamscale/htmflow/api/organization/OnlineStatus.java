package com.dreamscale.htmflow.api.organization;

public enum OnlineStatus {

    //Note, these are mapped to DB enum entries, so order is fixed in DB history
    Online(1), Offline(2);

    private final int order;

    OnlineStatus(int order) {
        this.order = order;
    }

    public int getOrder() {
        return this.order;
    }
}
