package com.dreamscale.htmflow.client;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.account.ActivationCodeDto;
import com.dreamscale.htmflow.api.account.AccountActivationDto;
import com.dreamscale.htmflow.api.account.HeartbeatDto;
import com.dreamscale.htmflow.api.account.SimpleStatusDto;
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
    SimpleStatusDto login();

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.LOGOUT_PATH)
    SimpleStatusDto logout();

}
