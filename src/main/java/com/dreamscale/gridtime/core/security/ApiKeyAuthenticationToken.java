package com.dreamscale.gridtime.core.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {

    private UUID rootAccountId;
    private Object credentials;

    public ApiKeyAuthenticationToken(UUID rootAccountId, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.rootAccountId = rootAccountId;
        this.credentials = credentials;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return rootAccountId;
    }

}
