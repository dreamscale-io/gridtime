package com.dreamscale.gridtime.core.capability.directory;

import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.invitation.InvitationDto;
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

    public InvitationDto useInvitationKey(UUID rootAccountId, String invitationKey) {

        LocalDateTime now = gridClock.now();

        OneTimeTicketEntity invitationTicket = oneTimeTicketCapability.findByTicketCode(invitationKey);

        validateInvitationKeyFound(invitationTicket);
        validateInvitationNotExpired(now, invitationTicket);

        InvitationDto invitationDto = new InvitationDto();
        invitationDto.setTicketType(invitationTicket.getTicketType().name());

        SimpleStatusDto status = processTicket(now, rootAccountId, invitationTicket);

        invitationDto.setStatus(status);

        return invitationDto;
    }

    private SimpleStatusDto joinWithInvite(UUID rootAccountId, OneTimeTicketEntity inviteTicket) {

        SimpleStatusDto simpleStatusDto = new SimpleStatusDto();

        LocalDateTime now = gridClock.now();

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

        switch (inviteTicket.getTicketType()) {

            case ACTIVATE_AND_INVITE_TO_ORG:
            case INVITE_TO_ORG:
                status = organizationCapability.joinOrganization(now, rootAccountId, organizationId, orgEmail);
                break;

            case ACTIVATE_AND_INVITE_TO_ORG_AND_TEAM:
                if (organizationCapability.isMember(rootAccountId, organizationId) == false) {
                    status = organizationCapability.joinOrganization(now, rootAccountId, organizationId, orgEmail);
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

    private void validateInvitationKeyFound(OneTimeTicketEntity invitationKeyTicket) {

        if (invitationKeyTicket == null) {
            throw new BadRequestException(ValidationErrorCodes.INVALID_OR_EXPIRED_INVITATION_KEY, "Invitation Key not found.");
        }

    }

    public SimpleStatusDto inviteToActiveOrganization(UUID invokingRootAccountId, String orgEmail) {
        return null;
    }

    public SimpleStatusDto inviteToActiveTeam(UUID invokingRootAccountId, String email) {
        return null;
    }

    public SimpleStatusDto inviteToPublicCommunity(UUID invokingRootAccountId, String email) {
        return null;
    }
}
