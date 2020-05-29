package com.dreamscale.gridtime.client;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.EmailInputDto;
import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.account.UsernameInputDto;
import feign.Headers;
import feign.RequestLine;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface InviteToClient {

    @RequestLine("POST " + ResourcePaths.INVITE_PATH + ResourcePaths.TO_PATH +
            ResourcePaths.OPEN_PATH)
    SimpleStatusDto inviteToPublicCommunity(EmailInputDto emailInputDto);

    @RequestLine("POST " + ResourcePaths.INVITE_PATH + ResourcePaths.TO_PATH +
            ResourcePaths.ORGANIZATION_PATH)
    SimpleStatusDto inviteToActiveOrganization(EmailInputDto emailInputDto);

    @RequestLine("POST " + ResourcePaths.INVITE_PATH + ResourcePaths.TO_PATH +
            ResourcePaths.TEAM_PATH + ResourcePaths.WITH_PATH + ResourcePaths.EMAIL_PATH)
    SimpleStatusDto inviteToActiveTeamWithEmail(EmailInputDto emailInputDto);

    @RequestLine("POST " + ResourcePaths.INVITE_PATH + ResourcePaths.TO_PATH +
            ResourcePaths.TEAM_PATH + ResourcePaths.WITH_PATH + ResourcePaths.USERNAME_PATH)
    SimpleStatusDto inviteToActiveTeamWithUsername(UsernameInputDto usernameInputDto);

}
