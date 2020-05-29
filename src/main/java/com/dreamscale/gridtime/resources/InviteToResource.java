package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.EmailInputDto;
import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.account.UsernameInputDto;
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
    @PostMapping(ResourcePaths.TO_PATH + ResourcePaths.OPEN_PATH)
    public SimpleStatusDto inviteToPublicCommunity(@RequestBody EmailInputDto emailInputDto ) {

        RequestContext context = RequestContext.get();
        log.info("inviteToPublicCommunity, user={}", context.getRootAccountId());

        return inviteCapability.inviteToPublicCommunityOrg(context.getRootAccountId(), emailInputDto.getEmail());
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
    public SimpleStatusDto inviteToActiveOrganization(@RequestBody EmailInputDto emailInputDto ) {

        RequestContext context = RequestContext.get();
        log.info("inviteToActiveOrganization, user={}", context.getRootAccountId());

        return inviteCapability.inviteToActiveOrganization(context.getRootAccountId(), emailInputDto.getEmail());
    }

    /**
     * Invites the user at the specified email to a team within the organization, by sending an email
     * with an Invitation Key associated with the organization join privilege, and the team join privilege, combined.
     *
     * Using this InvitationKey will cause the user to be joined to both the org, and the team.
     *
     * @return SimpleStatusDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.TO_PATH + ResourcePaths.TEAM_PATH + ResourcePaths.WITH_PATH + ResourcePaths.EMAIL_PATH)
    public SimpleStatusDto inviteToActiveTeamWithEmail(@RequestBody EmailInputDto emailInputDto ) {

        RequestContext context = RequestContext.get();
        log.info("inviteToActiveTeamWithEmail, user={}", context.getRootAccountId());

        return inviteCapability.inviteToActiveTeamWithEmail(context.getRootAccountId(), emailInputDto.getEmail());
    }

    /**
     * Invites the user with the specified org username to a team within the active organization.
     *
     * The user m
     *
     * Using this InvitationKey will cause the user to be joined to both the org, and the team.
     *
     * @return SimpleStatusDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.TO_PATH + ResourcePaths.TEAM_PATH + ResourcePaths.WITH_PATH + ResourcePaths.USERNAME_PATH)
    public SimpleStatusDto inviteToActiveTeamWithUsername(@RequestBody UsernameInputDto usernameInputDto ) {

        RequestContext context = RequestContext.get();
        log.info("inviteToActiveTeamWithUsername, user={}", context.getRootAccountId());

        return inviteCapability.inviteToActiveTeamWithUsername(context.getRootAccountId(), usernameInputDto.getUsername());
    }

}
