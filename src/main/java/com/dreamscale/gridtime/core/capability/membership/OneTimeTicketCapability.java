package com.dreamscale.gridtime.core.capability.membership;

import com.dreamscale.gridtime.core.domain.member.OneTimeTicketEntity;
import com.dreamscale.gridtime.core.domain.member.OneTimeTicketRepository;
import com.dreamscale.gridtime.core.domain.member.TicketType;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.commons.JSONTransformer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class OneTimeTicketCapability {

    @Autowired
    private OneTimeTicketRepository oneTimeTicketRepository;

    public OneTimeTicketEntity issueOneTimeActivationTicket(LocalDateTime now, UUID ticketOwnerId) {
        OneTimeTicketEntity oneTimeTicket = new OneTimeTicketEntity();
        oneTimeTicket.setId(UUID.randomUUID());
        oneTimeTicket.setOwnerId(ticketOwnerId);
        oneTimeTicket.setTicketType(TicketType.ACTIVATION_BY_OWNER);
        oneTimeTicket.setTicketCode(generateTicketCode());
        oneTimeTicket.setIssueDate(now);
        oneTimeTicket.setExpirationDate(now.plusDays(1));

        oneTimeTicketRepository.save(oneTimeTicket);
        return oneTimeTicket;
    }

    public OneTimeTicketEntity issueOneTimeActivateAndInviteTicket(LocalDateTime now, UUID ticketOwnerId, UUID organizationId, String orgEmail) {
        OneTimeTicketEntity oneTimeTicket = new OneTimeTicketEntity();
        oneTimeTicket.setId(UUID.randomUUID());
        oneTimeTicket.setOwnerId(ticketOwnerId);
        oneTimeTicket.setTicketType(TicketType.ACTIVATE_AND_INVITE_TO_ORG);
        oneTimeTicket.setTicketCode(generateTicketCode());
        oneTimeTicket.setIssueDate(now);
        oneTimeTicket.setExpirationDate(now.plusDays(1));

        Map<String, String> props = DefaultCollections.map();
        props.put(OneTimeTicketEntity.EMAIL_PROP, orgEmail);
        props.put(OneTimeTicketEntity.ORGANIZATION_ID_PROP, organizationId.toString());

        oneTimeTicket.setJsonProps(JSONTransformer.toJson(props));

        oneTimeTicketRepository.save(oneTimeTicket);
        return oneTimeTicket;
    }

    public OneTimeTicketEntity issueOneTimeActivateAndInviteToOrgAndTeamTicket(
            LocalDateTime now, UUID rootAccountId, UUID organizationId, UUID teamId, String orgEmail) {

        OneTimeTicketEntity oneTimeTicket = new OneTimeTicketEntity();
        oneTimeTicket.setId(UUID.randomUUID());
        oneTimeTicket.setOwnerId(rootAccountId);
        oneTimeTicket.setTicketType(TicketType.ACTIVATE_AND_INVITE_TO_ORG_AND_TEAM);
        oneTimeTicket.setTicketCode(generateTicketCode());
        oneTimeTicket.setIssueDate(now);
        oneTimeTicket.setExpirationDate(now.plusDays(1));

        Map<String, String> props = DefaultCollections.map();
        props.put(OneTimeTicketEntity.EMAIL_PROP, orgEmail);
        props.put(OneTimeTicketEntity.ORGANIZATION_ID_PROP, organizationId.toString());
        props.put(OneTimeTicketEntity.TEAM_ID_PROP, teamId.toString());

        oneTimeTicket.setJsonProps(JSONTransformer.toJson(props));

        oneTimeTicketRepository.save(oneTimeTicket);
        return oneTimeTicket;
    }


    public OneTimeTicketEntity issueOneTimeEmailValidationTicket(LocalDateTime now, UUID ticketOwnerId, String newEmail) {
        OneTimeTicketEntity oneTimeTicket = new OneTimeTicketEntity();
        oneTimeTicket.setId(UUID.randomUUID());
        oneTimeTicket.setOwnerId(ticketOwnerId);
        oneTimeTicket.setTicketType(TicketType.EMAIL_VALIDATION);
        oneTimeTicket.setTicketCode(generateTicketCode());
        oneTimeTicket.setIssueDate(now);
        oneTimeTicket.setExpirationDate(now.plusDays(1));

        Map<String, String> props = DefaultCollections.map();
        props.put(OneTimeTicketEntity.EMAIL_PROP, newEmail);

        oneTimeTicket.setJsonProps(JSONTransformer.toJson(props));

        oneTimeTicketRepository.save(oneTimeTicket);
        return oneTimeTicket;
    }

    public OneTimeTicketEntity issueOneTimeOrgEmailValidationTicket(LocalDateTime now, UUID ticketOwnerId, UUID organizationId, String orgEmail) {
        OneTimeTicketEntity oneTimeTicket = new OneTimeTicketEntity();
        oneTimeTicket.setId(UUID.randomUUID());
        oneTimeTicket.setOwnerId(ticketOwnerId);
        oneTimeTicket.setTicketType(TicketType.EMAIL_VALIDATION);
        oneTimeTicket.setTicketCode(generateTicketCode());
        oneTimeTicket.setIssueDate(now);
        oneTimeTicket.setExpirationDate(now.plusDays(1));

        Map<String, String> props = DefaultCollections.map();
        props.put(OneTimeTicketEntity.EMAIL_PROP, orgEmail);
        props.put(OneTimeTicketEntity.ORGANIZATION_ID_PROP, organizationId.toString());

        oneTimeTicket.setJsonProps(JSONTransformer.toJson(props));

        oneTimeTicketRepository.save(oneTimeTicket);
        return oneTimeTicket;
    }



    //

    private String generateTicketCode() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public OneTimeTicketEntity findByTicketCode(String ticketCode) {
       return oneTimeTicketRepository.findByTicketCode(ticketCode);
    }

    public void delete(OneTimeTicketEntity oneTimeTicket) {
        oneTimeTicketRepository.delete(oneTimeTicket);
    }

    public void update(OneTimeTicketEntity existingInvitation) {
        oneTimeTicketRepository.save(existingInvitation);
    }

    public boolean isExpired(LocalDateTime now, OneTimeTicketEntity oneTimeTicket) {
        if (oneTimeTicket != null) {
            return now.isAfter(oneTimeTicket.getExpirationDate());
        }
        return false;
    }



}
