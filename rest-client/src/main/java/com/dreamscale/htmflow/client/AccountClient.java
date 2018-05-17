package com.dreamscale.htmflow.client;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.account.*;
import feign.Headers;
import feign.RequestLine;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface AccountClient {

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.ACTIVATE_PATH)
    AccountActivationDto activate(ActivationCodeDto accountKey);

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.HEARTBEAT_PATH)
    SimpleStatusDto heartbeat(HeartbeatDto accountKey);

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.LOGIN_PATH)
    ConnectionStatusDto login();

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.LOGOUT_PATH)
    SimpleStatusDto logout();

}
