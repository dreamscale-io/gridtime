package com.dreamscale.gridtime.core.security;

import java.util.UUID;

public interface MasterAccountIdResolver {

	UUID findAccountIdByApiKey(String apiKey);

	UUID findAccountIdByConnectionId(String connectionId);

}
