package com.dreamscale.ideaflow;

import com.dreamscale.ideaflow.core.domain.MasterAccountEntity;
import com.dreamscale.ideaflow.core.security.MasterAccountIdResolver;

import java.util.UUID;

public class StubMasterAccountIdResolver implements MasterAccountIdResolver {

    private MasterAccountEntity user;

    public StubMasterAccountIdResolver(MasterAccountEntity user) {
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