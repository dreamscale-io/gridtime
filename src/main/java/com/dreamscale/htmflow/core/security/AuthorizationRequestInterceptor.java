package com.dreamscale.htmflow.core.security;

import com.dreamscale.htmflow.core.domain.member.MasterAccountEntity;
import feign.RequestInterceptor;
import feign.RequestTemplate;

public class AuthorizationRequestInterceptor implements RequestInterceptor {

    private final MasterAccountEntity activeAccount;

    public AuthorizationRequestInterceptor(MasterAccountEntity activeAccount) {
        this.activeAccount = activeAccount;
    }

    @Override
    public void apply(RequestTemplate template) {
        template.header(AuthHeaders.AUTHORIZATION, activeAccount.getApiKey());
    }
}