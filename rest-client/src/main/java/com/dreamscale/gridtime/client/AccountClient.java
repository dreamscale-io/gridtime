package com.dreamscale.gridtime.client;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.*;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.UUID;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface AccountClient {

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.REGISTER_PATH)
    UserProfileDto register(RootAccountCredentialsInputDto accountCreds);

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.ACTIVATE_PATH)
    AccountActivationDto activate(ActivationCodeDto accountKey);

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.RESET_PATH)
    SimpleStatusDto reset(RootAccountEmailInputDto accountEmailInputDto);

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.LOGIN_PATH)
    ConnectionStatusDto login();

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.LOGIN_PATH + ResourcePaths.WITH_PATH +  ResourcePaths.PASSWORD_PATH)
    ConnectionStatusDto loginWithPassword(RootLoginInputDto rootLoginInputDto);

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.LOGIN_PATH +
            ResourcePaths.TO_PATH + ResourcePaths.ORGANIZATION_PATH + "/{organizationId}")
    ConnectionStatusDto loginToOrganization(@Param("organizationId") UUID organizationId);

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.LOGIN_PATH +
            ResourcePaths.TO_PATH + ResourcePaths.ORGANIZATION_PATH + "/{organizationId}" + ResourcePaths.WITH_PATH + ResourcePaths.PASSWORD_PATH)
    ConnectionStatusDto loginToOrganizationWithPassword(@Param("organizationId") UUID organizationId, RootLoginInputDto rootLoginInputDto);

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.LOGOUT_PATH)
    SimpleStatusDto logout();

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.CONNECT_PATH)
    ActiveTalkConnectionDto connect(ConnectionInputDto connectionInputDto );

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.HEARTBEAT_PATH)
    SimpleStatusDto heartbeat(HeartbeatDto accountKey);

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.CYCLE_PATH + ResourcePaths.KEYS_PATH)
    AccountActivationDto cycleKeys();

    //profile things

    @RequestLine("GET " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.PROFILE_PATH)
    UserProfileDto getProfile();

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.PROFILE_PATH + ResourcePaths.ROOT_PATH + ResourcePaths.PROPERTY_PATH + ResourcePaths.USERNAME_PATH)
    UserProfileDto updateRootProfileUserName(UserNameInputDto userNameInputDto );

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.PROFILE_PATH + ResourcePaths.ROOT_PATH + ResourcePaths.PROPERTY_PATH + ResourcePaths.EMAIL_PATH)
    UserProfileDto updateRootProfileEmail(EmailInputDto emailInputDto );

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.PROFILE_PATH + ResourcePaths.ORG_PATH + ResourcePaths.PROPERTY_PATH + ResourcePaths.USERNAME_PATH)
    UserProfileDto updateOrgProfileUserName(UserNameInputDto userNameInputDto );

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.PROFILE_PATH + ResourcePaths.ORG_PATH + ResourcePaths.PROPERTY_PATH + ResourcePaths.EMAIL_PATH)
    UserProfileDto updateOrgProfileEmail(EmailInputDto emailInputDto );

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.PROFILE_PATH + ResourcePaths.ROOT_PATH + ResourcePaths.PROPERTY_PATH +
            ResourcePaths.EMAIL_PATH + ResourcePaths.VALIDATE_PATH + "?validationCode={validationCode}")
    SimpleStatusDto validateRootProfileEmail(@Param("validationCode") String validationCode );

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.PROFILE_PATH + ResourcePaths.ORG_PATH + ResourcePaths.PROPERTY_PATH +
            ResourcePaths.EMAIL_PATH + ResourcePaths.VALIDATE_PATH + "?validationCode={validationCode}")
    SimpleStatusDto validateOrgProfileEmail(@Param("validationCode") String validationCode );

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.PROFILE_PATH + ResourcePaths.ROOT_PATH + ResourcePaths.PROPERTY_PATH + ResourcePaths.FULLNAME_PATH)
    UserProfileDto updateRootProfileFullName(FullNameInputDto fullNameInputDto );

    @RequestLine("POST " + ResourcePaths.ACCOUNT_PATH + ResourcePaths.PROFILE_PATH +  ResourcePaths.ROOT_PATH + ResourcePaths.PROPERTY_PATH + ResourcePaths.DISPLAYNAME_PATH)
    UserProfileDto updateRootProfileDisplayName(DisplayNameInputDto displayNameInputDto );

}
