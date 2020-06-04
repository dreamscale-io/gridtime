package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.EmailInputDto;
import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.account.UsernameInputDto;
import com.dreamscale.gridtime.api.terminal.Command;
import com.dreamscale.gridtime.core.capability.membership.InviteCapability;
import com.dreamscale.gridtime.core.capability.terminal.TerminalRouteRegistry;
import com.dreamscale.gridtime.core.capability.terminal.TerminalRoute;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.security.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.INVITE_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class InviteToResource {

    @Autowired
    private InviteCapability inviteCapability;

    @Autowired
    private TerminalRouteRegistry terminalRouteRegistry;

    @PostConstruct
    void init() {
        terminalRouteRegistry.registerManPageDescription(Command.INVITE, "Invite people to your team, org, or the public community.");
        terminalRouteRegistry.register(Command.INVITE, new InviteTerminalRoute());
    }

    /**
     * Invites the user at the specified email to the public community space, by sending an email
     * with an Invitation Key associated with a basic account activation privilege.
     * <p>
     * This Invitation Key will cause the user to be joined to the public community.
     *
     * @return SimpleStatusDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.TO_PATH + ResourcePaths.PUBLIC_PATH)
    public SimpleStatusDto inviteToPublicCommunity(@RequestBody EmailInputDto emailInputDto) {

        RequestContext context = RequestContext.get();
        log.info("inviteToPublicOrg, user={}", context.getRootAccountId());

        return inviteCapability.inviteToPublicOrg(context.getRootAccountId(), emailInputDto.getEmail());
    }

    /**
     * Invites the user at the specified email to the organization, by sending an email
     * with an Invitation Key associated with the organization join privilege.
     * <p>
     * Using this InvitationKey will cause the user to be joined to the org.
     *
     * @return SimpleStatusDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.TO_PATH + ResourcePaths.ORGANIZATION_PATH)
    public SimpleStatusDto inviteToActiveOrganization(@RequestBody EmailInputDto emailInputDto) {

        RequestContext context = RequestContext.get();
        log.info("inviteToActiveOrganization, user={}", context.getRootAccountId());

        return inviteCapability.inviteToActiveOrganization(context.getRootAccountId(), emailInputDto.getEmail());
    }

    /**
     * Invites the user at the specified email to a team within the organization, by sending an email
     * with an Invitation Key associated with the organization join privilege, and the team join privilege, combined.
     * <p>
     * Using this InvitationKey will cause the user to be joined to both the org, and the team.
     *
     * @return SimpleStatusDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.TO_PATH + ResourcePaths.TEAM_PATH + ResourcePaths.WITH_PATH + ResourcePaths.EMAIL_PATH)
    public SimpleStatusDto inviteToActiveTeamWithEmail(@RequestBody EmailInputDto emailInputDto) {

        RequestContext context = RequestContext.get();
        log.info("inviteToActiveTeamWithEmail, user={}", context.getRootAccountId());

        return inviteCapability.inviteToActiveTeamWithEmail(context.getRootAccountId(), emailInputDto.getEmail());
    }

    /**
     * Invites the user with the specified org username to a team within the active organization.
     * <p>
     * The user m
     * <p>
     * Using this InvitationKey will cause the user to be joined to both the org, and the team.
     *
     * @return SimpleStatusDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.TO_PATH + ResourcePaths.TEAM_PATH + ResourcePaths.WITH_PATH + ResourcePaths.USERNAME_PATH)
    public SimpleStatusDto inviteToActiveTeamWithUsername(@RequestBody UsernameInputDto usernameInputDto) {

        RequestContext context = RequestContext.get();
        log.info("inviteToActiveTeamWithUsername, user={}", context.getRootAccountId());

        return inviteCapability.inviteToActiveTeamWithUsername(context.getRootAccountId(), usernameInputDto.getUsername());
    }


    private class InviteTerminalRoute extends TerminalRoute {

        private static final String USER_PARAM = "user";
        private static final String TARGET_PARAM = "target";

        private static final String ORG_TARGET_CHOICE = "org";
        private static final String TEAM_TARGET_CHOICE = "team";
        private static final String PUBLIC_TARGET_CHOICE = "public";

        InviteTerminalRoute() {
            super(Command.INVITE, "{" + USER_PARAM + "} to {" + TARGET_PARAM + "}");

            describeTextOption(USER_PARAM, "an email or username");
            describeChoiceOption(TARGET_PARAM, ORG_TARGET_CHOICE, TEAM_TARGET_CHOICE, PUBLIC_TARGET_CHOICE);
        }

        @Override
        public Object route(Map<String, String> params) {
            String user = params.get(USER_PARAM);
            String target = params.get(TARGET_PARAM);

            if (isEmail(user)) {

                EmailInputDto emailInputDto = new EmailInputDto(user);

                if (isPublic(target)) {
                    return inviteToPublicCommunity(emailInputDto);
                }

                if (isTeam(target)) {
                    return inviteToActiveTeamWithEmail(emailInputDto);
                }

                if (isOrg(target)) {
                    return inviteToActiveOrganization(emailInputDto);
                }

            } else { //if not email, then username

                UsernameInputDto usernameInputDto = new UsernameInputDto(user);

                if (isTeam(target)) {
                    return inviteToActiveTeamWithUsername(usernameInputDto);
                }
            }

            throw new BadRequestException(ValidationErrorCodes.UNABLE_TO_FIND_TERMINAL_ROUTE, "Unable to find a matching terminal command to execute");

        }

        private boolean isOrg(String target) {
            return (target != null) && target.equals(ORG_TARGET_CHOICE);
        }

        private boolean isTeam(String target) {
            return (target != null) && target.equals(TEAM_TARGET_CHOICE);
        }

        private boolean isPublic(String target) {
            return (target != null) && target.equals(PUBLIC_TARGET_CHOICE);
        }

        private boolean isEmail(String user) {
            return (user != null) && user.contains("@");
        }
    }


}
