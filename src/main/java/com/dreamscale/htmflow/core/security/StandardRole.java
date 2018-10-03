package com.dreamscale.htmflow.core.security;

public enum StandardRole {

    USER, ORG_ADMIN, SUPER_ADMIN;

    public String getName() {
        return "ROLE_" + this.name();
    }

}
