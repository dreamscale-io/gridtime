package com.dreamscale.gridtime.core.capability.directory;

import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.invitation.InvitationKeyDto;
import com.dreamscale.gridtime.api.status.Status;
import com.dreamscale.gridtime.core.capability.active.OneTimeTicketCapability;
import com.dreamscale.gridtime.core.domain.member.*;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.service.GridClock;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class InviteCapability {

    @Autowired
    private OrganizationCapability organizationCapability;

    @Autowired
    private TeamCapability teamCapability;

    @Autowired
    private OneTimeTicketCapability oneTimeTicketCapability;

    @Autowired
    private GridClock gridClock;


    public InvitationKeyDto useInvitationKey(UUID rootAccountId, String invitationKey) {

        LocalDateTime now = gridClock.now();

        OneTimeTicketEntity invitationTicket = oneTimeTicketCapability.findByTicketCode(invitationKey);

        validateInvitationKeyFound(invitationKey, invitationTicket);
        validateInvitationNotExpired(now, invitationTicket);

        InvitationKeyDto invitationKeyDto = new InvitationKeyDto();
        invitationKeyDto.setInvitationType(invitationTicket.getTicketType().name());

        SimpleStatusDto status = processInvite(now, rootAccountId, invitationTicket);

        invitationKeyDto.setStatus(status);
        invitationKeyDto.setKey(invitationKey);

        return invitationKeyDto;
    }

    private SimpleStatusDto processInvite(LocalDateTime now, UUID rootAccountId, OneTimeTicketEntity inviteTicket) {

        SimpleStatusDto simpleStatusDto = new SimpleStatusDto();

        if (inviteTicket == null) {
            simpleStatusDto.setMessage("Invitation Key not found.");
            simpleStatusDto.setStatus(Status.FAILED);
        } else if (oneTimeTicketCapability.isExpired(now, inviteTicket)) {
            simpleStatusDto.setMessage("Invitation Key is expired.");
            simpleStatusDto.setStatus(Status.FAILED);

            oneTimeTicketCapability.delete(inviteTicket);

        } else {
            simpleStatusDto = processTicket(now, rootAccountId, inviteTicket);
            oneTimeTicketCapability.delete(inviteTicket);
        }

        return simpleStatusDto;
    }

    private SimpleStatusDto processTicket(LocalDateTime now, UUID rootAccountId, OneTimeTicketEntity inviteTicket) {

        SimpleStatusDto status = new SimpleStatusDto();

        UUID organizationId = inviteTicket.getOrganizationIdProp();
        UUID teamId = inviteTicket.getTeamIdProp();
        String orgEmail = inviteTicket.getEmailProp();

        log.debug("Process invitation: type {}, org {}, team {}, email {}", inviteTicket.getTicketType().name(), organizationId, teamId, orgEmail);

        switch (inviteTicket.getTicketType()) {

            case ACTIVATE_AND_INVITE_TO_ORG:
            case INVITE_TO_ORG:
                status = organizationCapability.joinOrganization(now, rootAccountId, organizationId, orgEmail);
                break;

            case ACTIVATE_AND_INVITE_TO_ORG_AND_TEAM:
                if (organizationCapability.isMember(organizationId, rootAccountId) == false) {
                    organizationCapability.joinOrganization(now, rootAccountId, organizationId, orgEmail);
                }
            case INVITE_TO_TEAM:
                status = teamCapability.joinTeam(now, rootAccountId, organizationId, teamId);
                break;
            case INVITE_TO_ROOM:
                //TODO invite to room capability
                break;
        }

        return status;
    }

    private void validateInvitationNotExpired(LocalDateTime now, OneTimeTicketEntity invitationTicket) {
        if (oneTimeTicketCapability.isExpired(now, invitationTicket)) {
            throw new BadRequestException(ValidationErrorCodes.INVALID_OR_EXPIRED_INVITATION_KEY, "Invitation Key is expired.");
        }
    }

    private void validateInvitationKeyFound(String invitationKey, OneTimeTicketEntity invitationKeyTicket) {

        if (invitationKeyTicket == null) {
            throw new BadRequestException(ValidationErrorCodes.INVALID_OR_EXPIRED_INVITATION_KEY, "Invitation Key '"+invitationKey+"' not found.");
        }
    }

    public SimpleStatusDto inviteToActiveOrganization(UUID invokingRootAccountId, String orgEmail) {

        return organizationCapability.inviteToOrganizationWithEmail(invokingRootAccountId, orgEmail);
    }

    public SimpleStatusDto inviteToActiveTeamWithEmail(UUID invokingRootAccountId, String email) {

        return organizationCapability.inviteToOrganizationAndTeamWithEmail(invokingRootAccountId, email);
    }


    public SimpleStatusDto inviteToActiveTeamWithUsername(UUID rootAccountId, String userToInvite) {

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(rootAccountId);
        return teamCapability.inviteUserToMyActiveTeam(invokingMember.getOrganizationId(), invokingMember.getId(), userToInvite);
    }


    public SimpleStatusDto inviteToPublicCommunity(UUID invokingRootAccountId, String email) {
        return null;
    }

}
