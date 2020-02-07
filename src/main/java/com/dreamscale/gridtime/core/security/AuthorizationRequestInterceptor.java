package com.dreamscale.gridtime.core.security;

import com.dreamscale.gridtime.core.domain.member.RootAccountEntity;
import feign.RequestInterceptor;
import feign.RequestTemplate;

public class AuthorizationRequestInterceptor implements RequestInterceptor {

    private final RootAccountEntity activeAccount;

    public AuthorizationRequestInterceptor(RootAccountEntity activeAccount) {
        this.activeAccount = activeAccount;
    }

    @Override
    public void apply(RequestTemplate template) {
        template.header(AuthHeaders.AUTHORIZATION, activeAccount.getApiKey());
    }
}