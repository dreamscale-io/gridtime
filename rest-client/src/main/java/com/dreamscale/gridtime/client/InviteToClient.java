package com.dreamscale.gridtime.client;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.EmailInputDto;
import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.circuit.ChatMessageInputDto;
import com.dreamscale.gridtime.api.circuit.ScreenshotReferenceInputDto;
import com.dreamscale.gridtime.api.circuit.TalkMessageDto;
import com.dreamscale.gridtime.api.flow.event.NewSnippetEventDto;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface InviteToClient {

    @RequestLine("POST " + ResourcePaths.INVITE_PATH + ResourcePaths.TO_PATH +
            ResourcePaths.PUBLIC_PATH)
    SimpleStatusDto inviteToPublicCommunity(EmailInputDto emailInputDto);

    @RequestLine("POST " + ResourcePaths.INVITE_PATH + ResourcePaths.TO_PATH +
            ResourcePaths.ORGANIZATION_PATH)
    SimpleStatusDto inviteToActiveOrganization(EmailInputDto emailInputDto);

    @RequestLine("POST " + ResourcePaths.INVITE_PATH + ResourcePaths.TO_PATH +
            ResourcePaths.TEAM_PATH)
    SimpleStatusDto inviteToActiveTeam(EmailInputDto emailInputDto);

}
