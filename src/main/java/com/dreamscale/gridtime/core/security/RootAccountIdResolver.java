package com.dreamscale.gridtime.core.security;

import java.util.UUID;

public interface RootAccountIdResolver {

	UUID findAccountIdByApiKey(String apiKey);

	UUID findAccountIdByConnectionId(String connectionId);

}
