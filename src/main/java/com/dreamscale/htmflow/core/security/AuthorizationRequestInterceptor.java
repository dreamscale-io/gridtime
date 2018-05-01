package com.dreamscale.htmflow.core.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class AuthorizationRequestInterceptor implements RequestInterceptor {

    private String staticHeader;

    public AuthorizationRequestInterceptor(String staticHeader) {
        this.staticHeader = staticHeader;
    }

    @Override
    public void apply(RequestTemplate template) {
        template.header(AuthHeaders.AUTHORIZATION, staticHeader);
    }
}