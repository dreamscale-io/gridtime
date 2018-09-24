package com.dreamscale.htmflow.core.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {

    private UUID masterAccountId;
    private Object credentials;

    public ApiKeyAuthenticationToken(UUID masterAccountId, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.masterAccountId = masterAccountId;
        this.credentials = credentials;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return masterAccountId;
    }

}
