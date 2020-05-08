package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.invitation.InvitationKeyDto;
import com.dreamscale.gridtime.api.invitation.InvitationKeyInputDto;
import com.dreamscale.gridtime.core.capability.directory.InviteCapability;
import com.dreamscale.gridtime.core.security.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.INVITATION_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class InvitationResource {

    @Autowired
    private InviteCapability inviteCapability;

    /**
     * Process an existing Invitation Key.
     *
     * Using the Invitation Key will cause the user to be joined to whatever groups
     * are associated with the specified key.
     *
     * @return OrganizationDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping()
    public InvitationKeyDto useInvitationKey(@RequestBody InvitationKeyInputDto invitationKeyInputDto ) {

        RequestContext context = RequestContext.get();
        log.info("useInvitationKey, user={}", context.getRootAccountId());

        return inviteCapability.useInvitationKey(context.getRootAccountId(), invitationKeyInputDto.getInvitationKey());
    }

}
