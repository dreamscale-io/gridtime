package com.dreamscale.gridtime.client;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.EmailInputDto;
import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.invitation.InvitationDto;
import com.dreamscale.gridtime.api.invitation.InvitationKeyInputDto;
import feign.Headers;
import feign.RequestLine;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface InvitationClient {
    
    @RequestLine("POST " + ResourcePaths.INVITATION_PATH)
    InvitationDto useInvitationKey(InvitationKeyInputDto invitationKeyInputDto);

}
