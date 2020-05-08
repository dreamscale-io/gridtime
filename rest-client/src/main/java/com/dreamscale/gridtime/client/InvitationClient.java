package com.dreamscale.gridtime.client;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.invitation.InvitationKeyDto;
import com.dreamscale.gridtime.api.invitation.InvitationKeyInputDto;
import feign.Headers;
import feign.RequestLine;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface InvitationClient {
    
    @RequestLine("POST " + ResourcePaths.INVITATION_PATH)
    InvitationKeyDto useInvitationKey(InvitationKeyInputDto invitationKeyInputDto);

}
