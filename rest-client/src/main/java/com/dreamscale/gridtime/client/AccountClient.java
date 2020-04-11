package com.dreamscale.gridtime.client;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.*;
import com.dreamscale.gridtime.api.journal.RecentJournalDto;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface AccountClient {

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.REGISTER_PATH)
    SimpleStatusDto register(RootAccountCredentialsInputDto accountCreds);

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.ACTIVATE_PATH)
    AccountActivationDto activate(ActivationCodeDto accountKey);

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.RESET_PATH)
    SimpleStatusDto reset(RootAccountEmailInputDto accountEmailInputDto);

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.LOGIN_PATH)
    ConnectionStatusDto login();

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.LOGOUT_PATH)
    SimpleStatusDto logout();

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.CONNECT_PATH)
    RoomConnectionScopeDto connect(ConnectionInputDto connectionInputDto );

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.HEARTBEAT_PATH)
    SimpleStatusDto heartbeat(HeartbeatDto accountKey);

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.CYCLE_PATH + ResourcePaths.KEYS_PATH)
    AccountActivationDto cycleKeys();

    //profile things

    @RequestLine("GET " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.PROFILE_PATH)
    UserProfileDto getProfile();

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.PROFILE_PATH + ResourcePaths.PROPERTY_PATH + ResourcePaths.USERNAME_PATH)
    UserProfileDto updateProfileUserName(UserNameInputDto userNameInputDto );

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.PROFILE_PATH + ResourcePaths.PROPERTY_PATH + ResourcePaths.EMAIL_PATH)
    UserProfileDto updateProfileEmail(EmailInputDto emailInputDto );

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.PROFILE_PATH + ResourcePaths.PROPERTY_PATH +
            ResourcePaths.EMAIL_PATH + ResourcePaths.VALIDATE_PATH + "?validationCode={validationCode}")
    SimpleStatusDto validateProfileEmail(@Param("validationCode") String validationCode );

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.PROFILE_PATH + ResourcePaths.PROPERTY_PATH + ResourcePaths.FULLNAME_PATH)
    UserProfileDto updateProfileFullName(FullNameInputDto fullNameInputDto );

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.PROFILE_PATH + ResourcePaths.PROPERTY_PATH + ResourcePaths.DISPLAYNAME_PATH)
    UserProfileDto updateProfileDisplayName(DisplayNameInputDto displayNameInputDto );


}
