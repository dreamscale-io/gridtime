package com.dreamscale.htmflow.core.security;

import java.util.UUID;

public interface UserIdResolver {

	UUID findAccountIdByApiKey(String apiKey);

}
