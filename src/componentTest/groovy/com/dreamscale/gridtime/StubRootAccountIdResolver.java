package com.dreamscale.gridtime;

import com.dreamscale.gridtime.core.domain.member.RootAccountEntity;
import com.dreamscale.gridtime.core.security.RootAccountIdResolver;

import java.util.UUID;

public class StubRootAccountIdResolver implements RootAccountIdResolver {

    private RootAccountEntity user;

    public StubRootAccountIdResolver(RootAccountEntity user) {
        this.user = user;
    }

    @Override
    public UUID findAccountIdByApiKey(String apiKey) {
        return user.getApiKey().equals(apiKey) ? user.getId() : null;
    }

    @Override
    public UUID findAccountIdByConnectionId(String connectionId) {
        return user.getId();
    }

}