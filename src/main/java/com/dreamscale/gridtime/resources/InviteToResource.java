package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.EmailInputDto;
import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.invitation.InvitationDto;
import com.dreamscale.gridtime.api.invitation.InvitationKeyInputDto;
import com.dreamscale.gridtime.core.capability.directory.InviteCapability;
import com.dreamscale.gridtime.core.security.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.INVITE_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class InviteToResource {

    @Autowired
    private InviteCapability inviteCapability;

    /**
     * Invites the user at the specified email to the public community space, by sending an email
     * with an Invitation Key associated with a basic account activation privilege.
     *
     * This Invitation Key will cause the user to be joined to the public community.
     *
     * @return SimpleStatusDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.TO_PATH + ResourcePaths.PUBLIC_PATH)
    public SimpleStatusDto inviteToPublicCommunity(EmailInputDto emailInputDto ) {

        RequestContext context = RequestContext.get();
        log.info("inviteToPublicCommunity, user={}", context.getRootAccountId());

        return inviteCapability.inviteToPublicCommunity(context.getRootAccountId(), emailInputDto.getEmail());
    }

    /**
     * Invites the user at the specified email to the organization, by sending an email
     * with an Invitation Key associated with the organization join privilege.
     *
     * Using this InvitationKey will cause the user to be joined to the org.
     *
     * @return SimpleStatusDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.TO_PATH + ResourcePaths.ORGANIZATION_PATH)
    public SimpleStatusDto inviteToActiveOrganization(EmailInputDto emailInputDto ) {

        RequestContext context = RequestContext.get();
        log.info("inviteToActiveOrganization, user={}", context.getRootAccountId());

        return inviteCapability.inviteToActiveOrganization(context.getRootAccountId(), emailInputDto.getEmail());
    }

    /**
     * Invites the user at the specified email to a team within the organization, by sending an email
     * with an Invitation Key associated with the organization join privilege, and the team join privilege.
     *
     * Using this InvitationKey will cause the user to be joined to both the org, and the team.
     *
     * @return SimpleStatusDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.TO_PATH + ResourcePaths.TEAM_PATH)
    public SimpleStatusDto inviteToActiveTeam(EmailInputDto emailInputDto ) {

        RequestContext context = RequestContext.get();
        log.info("inviteToActiveTeam, user={}", context.getRootAccountId());

        return inviteCapability.inviteToActiveTeam(context.getRootAccountId(), emailInputDto.getEmail());
    }

}
