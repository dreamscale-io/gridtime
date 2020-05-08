package com.dreamscale.gridtime.core.capability.directory;

import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.organization.*;
import com.dreamscale.gridtime.api.status.ConnectionResultDto;
import com.dreamscale.gridtime.api.status.Status;
import com.dreamscale.gridtime.core.capability.active.OneTimeTicketCapability;
import com.dreamscale.gridtime.core.capability.integration.EmailCapability;
import com.dreamscale.gridtime.core.capability.integration.JiraCapability;
import com.dreamscale.gridtime.core.domain.active.ActiveAccountStatusRepository;
import com.dreamscale.gridtime.core.domain.member.*;
import com.dreamscale.gridtime.core.exception.ConflictErrorCodes;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.mapper.DtoEntityMapper;
import com.dreamscale.gridtime.core.mapper.MapperFactory;
import com.dreamscale.gridtime.core.service.GridClock;
import com.dreamscale.gridtime.core.service.MemberDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.Local;
import org.dreamscale.exception.BadRequestException;
import org.dreamscale.exception.ConflictException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class OrganizationCapability {
    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    OrganizationMemberRepository organizationMemberRepository;

    @Autowired
    private OrganizationInviteTokenRepository inviteTokenRepository;

    @Autowired
    private OrganizationMemberRepository memberRepository;

    @Autowired
    private RootAccountRepository rootAccountRepository;

    @Autowired
    private ActiveAccountStatusRepository activeAccountStatusRepository;

    @Autowired
    private TeamCapability teamCapability;


    @Autowired
    private OrganizationSubscriptionRepository organizationSubscriptionRepository;

    @Autowired
    private OrganizationSubscriptionDetailsRepository organizationSubscriptionDetailsRepository;

    @Autowired
    private JiraCapability jiraCapability;

    @Autowired
    private EmailCapability emailCapability;

    @Autowired
    private MemberDetailsService memberDetailsService;

    @Autowired
    private OneTimeTicketCapability oneTimeTicketCapability;

    @Autowired
    private OrganizationSubscriptionSeatRepository organizationSubscriptionSeatRepository;

    @Autowired
    private OrganizationMemberTombstoneRepository organizationMemberTombstoneRepository;

    @Autowired
    private GridClock gridClock;

    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<SubscriptionInputDto, OrganizationEntity> orgInputMapper;
    private DtoEntityMapper<OrganizationDto, OrganizationEntity> orgOutputMapper;
    private DtoEntityMapper<OrganizationSubscriptionDto, OrganizationSubscriptionDetailsEntity> subscriptionMapper;

    private static final String PUBLIC_ORG_DOMAIN = "public.dreamscale.io";
    private static final String PUBLIC_ORG_NAME = "Public";

    @PostConstruct
    private void init() {
        orgInputMapper = mapperFactory.createDtoEntityMapper(SubscriptionInputDto.class, OrganizationEntity.class);
        orgOutputMapper = mapperFactory.createDtoEntityMapper(OrganizationDto.class, OrganizationEntity.class);

        subscriptionMapper = mapperFactory.createDtoEntityMapper(OrganizationSubscriptionDto.class, OrganizationSubscriptionDetailsEntity.class);
    }

    public OrganizationDto getOrganizationByDomainName(String domainName) {
        OrganizationEntity orgEntity = organizationRepository.findByDomainName(domainName);
        return orgOutputMapper.toApi(orgEntity);
    }

    @Transactional
    public OrganizationSubscriptionDto createOrganizationSubscription(UUID rootAccountId, SubscriptionInputDto orgInputDto) {

        validateNotNull("orgName", orgInputDto.getOrganizationName());
        validateNotNull("domainName", orgInputDto.getDomainName());
        validateNotNull("seats", orgInputDto.getSeats());

        validateDomainNotAlreadyRegistered(orgInputDto.getDomainName());

        //so now I've got a new subscription object as part of my org, that's going to have some new fields, related to this payment thing

        LocalDateTime now = gridClock.now();

        OrganizationEntity organizationEntity = new OrganizationEntity();
        organizationEntity.setId(UUID.randomUUID());
        organizationEntity.setOrgName(orgInputDto.getOrganizationName());
        organizationEntity.setDomainName(orgInputDto.getDomainName());

        organizationRepository.save(organizationEntity);

        teamCapability.createEveryoneTeam(organizationEntity.getId());

        OrganizationSubscriptionEntity subscriptionEntity = new OrganizationSubscriptionEntity();

        subscriptionEntity.setId(UUID.randomUUID());
        subscriptionEntity.setOrganizationId(organizationEntity.getId());
        subscriptionEntity.setRootAccountOwnerId(rootAccountId);
        subscriptionEntity.setCreationDate(now);
        subscriptionEntity.setLastModifiedDate(now);
        subscriptionEntity.setLastStatusCheck(now);
        subscriptionEntity.setRequireMemberEmailInDomain(orgInputDto.getRequireMemberEmailInDomain());
        subscriptionEntity.setTotalSeats(orgInputDto.getSeats());
        subscriptionEntity.setSeatsRemaining(orgInputDto.getSeats());

        //TODO validate the payment in stripe, and get the associated customer id to save

        subscriptionEntity.setStripePaymentId(orgInputDto.getStripePaymentId());
        subscriptionEntity.setSubscriptionStatus(SubscriptionStatus.VALID);

        organizationSubscriptionRepository.save(subscriptionEntity);

        //TODO expire the orgs invitation token, need to get the expiration based on stripe payment
        //TODO memberships need to stop working when subscription is invalid, invitation token should expire too.

        LocalDateTime expiration = now.plusMonths(1);
        OrganizationInviteTokenEntity inviteToken = createInviteToken(organizationEntity.getId(), expiration);
        inviteTokenRepository.save(inviteToken);

        return createSubscriptionDto(organizationEntity, subscriptionEntity, inviteToken);

    }


    public List<OrganizationSubscriptionDto> getOrganizationSubscriptions(UUID rootAccountId) {

        List<OrganizationSubscriptionDetailsEntity> subscriptions = organizationSubscriptionDetailsRepository.findByRootAccountOwnerIdOrderByCreationDate(rootAccountId);

        return subscriptionMapper.toApiList(subscriptions);
    }

    @Transactional
    public SimpleStatusDto joinOrganizationWithInvitationAndEmail(UUID rootAccountId, JoinRequestInputDto joinRequestInputDto) {

        OrganizationInviteTokenEntity tokenEntity = inviteTokenRepository.findByToken(joinRequestInputDto.getInviteToken());

        LocalDateTime now = gridClock.now();

        validateTokenFound(now, joinRequestInputDto.getInviteToken(), tokenEntity);

        OrganizationEntity org = organizationRepository.findById(tokenEntity.getOrganizationId());
        OrganizationSubscriptionEntity subscription = organizationSubscriptionRepository.findByOrganizationId(tokenEntity.getOrganizationId());

        String standardizedEmail = joinRequestInputDto.getOrgEmail().toLowerCase();

        if (requiresEmailMatch(subscription)) {
            validateEmailWithinDomain(standardizedEmail, org.getDomainName());
        }

        RootAccountEntity rootAccount = rootAccountRepository.findById(rootAccountId);

        SimpleStatusDto statusDto = new SimpleStatusDto();
        if (rootAccount.getRootEmail().equals(standardizedEmail)) {
            createOrgMembership(now, subscription.getOrganizationId(), rootAccountId, standardizedEmail);

            statusDto.setStatus(Status.JOINED);
            statusDto.setMessage("Member added to organization.");
        } else {

            OneTimeTicketEntity oneTimeTicket = oneTimeTicketCapability.issueOneTimeOrgEmailValidationTicket(now, rootAccountId, org.getId(), standardizedEmail);
            //need to valid
            return emailCapability.sendEmailToValidateOrgEmailAddress(standardizedEmail, oneTimeTicket.getTicketCode());

        }
        return statusDto;
    }


    @Transactional
    public SimpleStatusDto inviteToOrganizationWithEmail(UUID rootAccountId, String email) {

        OrganizationDto org = getActiveOrganization(rootAccountId);

        OrganizationSubscriptionEntity subscription = organizationSubscriptionRepository.findByOrganizationId(org.getId());

        validateNotNull("email", email);
        validateSubscriptionFound(subscription);
        validateSubscriptionOwnedByRootAccount(subscription, rootAccountId);

        LocalDateTime now = gridClock.now();

        String standardizedEmail = email.toLowerCase();

        if (requiresEmailMatch(subscription)) {
            validateEmailWithinDomain(standardizedEmail, org.getDomainName());
        }

        //see if this person is already a member

        validateNoExistingMembership(org.getId(), standardizedEmail);

        //send invite or join email

        OneTimeTicketEntity oneTimeTicket = oneTimeTicketCapability.issueOneTimeActivateAndInviteTicket(now, rootAccountId, org.getId(), standardizedEmail);

        //so this code could come back through, an activation, or this "invite key" API, that needs to allow invite to anything

        //invite XXX to org
        //invite XXX to public
        //TODO enable the code use for activation

        return emailCapability.sendDownloadActivateAndOrgInviteEmail(standardizedEmail, org, oneTimeTicket.getTicketCode());
    }


    public SimpleStatusDto validateMemberEmailAndJoin(String validationCode) {

        OneTimeTicketEntity oneTimeTicket = oneTimeTicketCapability.findByTicketCode(validationCode);

        SimpleStatusDto simpleStatusDto = new SimpleStatusDto();

        LocalDateTime now = gridClock.now();

        if (oneTimeTicket == null) {
            simpleStatusDto.setMessage("Validation code not found.");
            simpleStatusDto.setStatus(Status.FAILED);
        } else if (oneTimeTicketCapability.isExpired(now, oneTimeTicket)) {
            simpleStatusDto.setMessage("Validation code is expired.");
            simpleStatusDto.setStatus(Status.FAILED);

            oneTimeTicketCapability.delete(oneTimeTicket);

        } else {

            UUID rootAccountId = oneTimeTicket.getOwnerId();
            String orgEmail = oneTimeTicket.getEmailProp();
            UUID organizationId = oneTimeTicket.getOrganizationIdProp();

            createOrgMembership(now, organizationId, rootAccountId, orgEmail);

            simpleStatusDto.setStatus(Status.JOINED);
            simpleStatusDto.setMessage("Member has joined the organization.");

            oneTimeTicketCapability.delete(oneTimeTicket);
        }

        return simpleStatusDto;
    }

    public SimpleStatusDto joinOrganization(LocalDateTime now, UUID rootAccountId, UUID organizationId, String orgEmail) {

        SimpleStatusDto status = new SimpleStatusDto();

        createOrgMembership(now, organizationId, rootAccountId, orgEmail);

        status.setStatus(Status.JOINED);
        status.setMessage("Member has joined the organization.");

        return status;
    }

    public OrganizationEntity findOrCreatePublicOrg() {

        OrganizationEntity publicOrg = organizationRepository.findByDomainName(PUBLIC_ORG_DOMAIN);

        if (publicOrg == null) {
            publicOrg = new OrganizationEntity();
            publicOrg.setId(UUID.randomUUID());
            publicOrg.setDomainName(PUBLIC_ORG_DOMAIN);
            publicOrg.setOrgName(PUBLIC_ORG_NAME);

            organizationRepository.save(publicOrg);
        }

        return publicOrg;
    }

    public boolean isPublicOrg(OrganizationEntity org) {
        if (org != null && org.getDomainName().equals(PUBLIC_ORG_DOMAIN)) {
            return true;
        }
        return false;
    }

    private OrganizationMemberEntity createOrgMembership(LocalDateTime now, UUID organizationId, UUID rootAccountId, String email) {

        OrganizationSubscriptionEntity subscription = reserveSeatForUpdate(organizationId);

        validateAvailableSeat(subscription);

        OrganizationMemberEntity membership = organizationMemberRepository.findByOrganizationIdAndRootAccountId(organizationId, rootAccountId);

        if (membership == null) {

            subscription.setSeatsRemaining(subscription.getSeatsRemaining() - 1);
            organizationSubscriptionRepository.save(subscription);

            OrganizationSubscriptionSeatEntity subscriptionSeat = new OrganizationSubscriptionSeatEntity();
            subscriptionSeat.setId(UUID.randomUUID());
            subscriptionSeat.setOrganizationId(organizationId);
            subscriptionSeat.setRootAccountId(rootAccountId);
            subscriptionSeat.setOrgEmail(email);
            subscriptionSeat.setSubscriptionId(subscription.getId());
            subscriptionSeat.setActivationDate(now);
            subscriptionSeat.setSubscriptionStatus(SubscriptionStatus.VALID);

            organizationSubscriptionSeatRepository.save(subscriptionSeat);

            membership = new OrganizationMemberEntity();
            membership.setId(UUID.randomUUID());
            membership.setOrganizationId(organizationId);
            membership.setRootAccountId(rootAccountId);
            membership.setEmail(email);

            organizationMemberRepository.save(membership);

            teamCapability.addMemberToEveryone(organizationId, membership.getId());
        } else {
            throw new ConflictException(ConflictErrorCodes.ACCOUNT_ALREADY_ADDED, "Account already added to this organization as a member.");
        }

        return membership;

    }

    private OrganizationSubscriptionEntity reserveSeatForUpdate(UUID organizationId) {

        return organizationSubscriptionRepository.selectOrgSubscriptionForUpdate(organizationId);

    }

    private void validateAvailableSeat(OrganizationSubscriptionEntity subscription) {
        if (subscription.getSeatsRemaining() <= 0) {
            throw new ConflictException(ConflictErrorCodes.NO_SEATS_AVAILABLE, "No seats available, check with your admin about increasing capacity.");
        }
    }

    private void validateNoExistingMembership(UUID organizationId, String standardizedEmail) {

        OrganizationMemberEntity existingMembership = organizationMemberRepository.findByOrganizationIdAndEmail(organizationId, standardizedEmail);

        if (existingMembership != null) {
            throw new ConflictException(ConflictErrorCodes.MEMBER_ALREADY_ADDED, "Member " + standardizedEmail + " already added to organization.");

        }

    }

    private void validateEmailWithinDomain(String standardizedEmail, String domainName) {
        if (!standardizedEmail.endsWith(domainName)) {
            throw new BadRequestException(ValidationErrorCodes.EMAIL_NOT_IN_DOMAIN, "Email '" + standardizedEmail + "' must be in the domain.");
        }
    }

    private boolean requiresEmailMatch(OrganizationSubscriptionEntity subscription) {
        return subscription.getRequireMemberEmailInDomain();
    }

    private void validateTokenFound(LocalDateTime now, String inviteToken, OrganizationInviteTokenEntity tokenEntity) {
        if (tokenEntity == null || isExpired(now, tokenEntity)) {
            throw new BadRequestException(ValidationErrorCodes.INVALID_OR_EXPIRED_INVITATION_KEY, "Invite token '" + inviteToken + "' is expired or not found.");
        }
    }


    private boolean isExpired(LocalDateTime now, OrganizationInviteTokenEntity inviteTokenEntity) {
        return now.isAfter(inviteTokenEntity.getExpirationDate());
    }


    private OrganizationSubscriptionDto createSubscriptionDto(OrganizationEntity organizationEntity, OrganizationSubscriptionEntity subscriptionEntity, OrganizationInviteTokenEntity inviteToken) {
        OrganizationSubscriptionDto subscriptionDto = new OrganizationSubscriptionDto();

        subscriptionDto.setId(subscriptionEntity.getId());
        subscriptionDto.setOrganizationId(organizationEntity.getId());
        subscriptionDto.setDomainName(organizationEntity.getDomainName());
        subscriptionDto.setOrganizationName(organizationEntity.getOrgName());
        subscriptionDto.setInviteToken(inviteToken.getToken());
        subscriptionDto.setRequireMemberEmailInDomain(subscriptionEntity.getRequireMemberEmailInDomain());
        subscriptionDto.setTotalSeats(subscriptionEntity.getTotalSeats());
        subscriptionDto.setSeatsRemaining(subscriptionEntity.getSeatsRemaining());
        subscriptionDto.setCreationDate(subscriptionEntity.getCreationDate());
        subscriptionDto.setSubscriptionStatus(subscriptionEntity.getSubscriptionStatus());

        return subscriptionDto;
    }


    private void validateNotNull(String fieldName, Object value) {
        if (value == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_FIELD, "Field '" + fieldName + "' is required.");
        }
    }

    private void validateDomainNotAlreadyRegistered(String domainName) {
        String standardizedDomain = domainName.toLowerCase();

        OrganizationEntity existingOrg = organizationRepository.findByDomainName(standardizedDomain);

        if (existingOrg != null) {
            throw new ConflictException(ConflictErrorCodes.ORG_DOMAIN_ALREADY_IN_USE, "Org domain '" + domainName + "' already in use.");
        }
    }


    private OrganizationInviteTokenEntity createInviteToken(UUID organizationId, LocalDateTime expiration) {
        OrganizationInviteTokenEntity inviteToken = new OrganizationInviteTokenEntity();
        inviteToken.setOrganizationId(organizationId);
        inviteToken.setId(UUID.randomUUID());
        inviteToken.setToken(generateToken());
        inviteToken.setExpirationDate(expiration);

        return inviteToken;
    }


    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }


    public OrganizationMemberEntity getDefaultOrganizationMembership(UUID rootAccountId) {

        List<OrganizationEntity> orgs = organizationRepository.findByParticipatingMembership(rootAccountId);

        OrganizationEntity priorityOrg = findPriorityOrg(orgs);

        return organizationMemberRepository.findByOrganizationIdAndRootAccountId(priorityOrg.getId(), rootAccountId);
    }

    private OrganizationEntity findPriorityOrg(List<OrganizationEntity> orgs) {
        OrganizationEntity priorityOrg = null;

        for (OrganizationEntity org : orgs) {
            if (priorityOrg == null && isPublicOrg(org)) {
                priorityOrg = org;
            } else if (!isPublicOrg(org)) {
                priorityOrg = org;
            }
        }

        return priorityOrg;
    }

    public OrganizationMemberEntity getActiveMembership(UUID rootAccountId) {
        OrganizationMemberEntity activeMembership = memberRepository.findByActiveOrganizationAndRootAccountId(rootAccountId);

        if (activeMembership == null) {
            activeMembership = getDefaultOrganizationMembership(rootAccountId);
        }

        if (activeMembership == null) {
            throw new BadRequestException(ValidationErrorCodes.NO_ORG_MEMBERSHIP_FOR_ACCOUNT, "active organization membership not found");
        }

        return activeMembership;
    }

    public OrganizationDto getActiveOrganization(UUID rootAccountId) {

        OrganizationMemberEntity activeMembership = getActiveMembership(rootAccountId);

        OrganizationEntity organizationEntity = organizationRepository.findById(activeMembership.getOrganizationId());
        return orgOutputMapper.toApi(organizationEntity);
    }

    public List<OrganizationDto> getParticipatingOrganizations(UUID rootAccountId) {

        List<OrganizationEntity> orgs = organizationRepository.findByParticipatingMembership(rootAccountId);

        return orgOutputMapper.toApiList(orgs);
    }

    public List<MemberDetailsDto> getMembersOfActiveOrganization(UUID rootAccountId) {
        OrganizationDto activeOrganization = getActiveOrganization(rootAccountId);

        UUID organizationId = activeOrganization.getId();

        OrganizationSubscriptionEntity subscription = findAndValidateSubscription(rootAccountId, organizationId);
        //then, and only then...

        return memberDetailsService.getOrganizationMembers(organizationId);
    }

    private OrganizationSubscriptionEntity findAndValidateSubscription(UUID rootAccountId, UUID organizationId) {
        OrganizationSubscriptionEntity subscription = organizationSubscriptionRepository.findByOrganizationId(organizationId);

        validateSubscriptionFound(subscription);

        validateSubscriptionOwnedByRootAccount(subscription, rootAccountId);

        return subscription;
    }

    public boolean isMember(UUID organizationId, UUID rootAccountId) {

        OrganizationMemberEntity member = organizationMemberRepository.findByOrganizationIdAndRootAccountId(organizationId, rootAccountId);

        if (member != null) {
            return true;
        }
        return false;
    }

    @Transactional
    public SimpleStatusDto removeMember(UUID requestingRootAccountId, UUID memberId) {

        //first, lets take care of the organization subscription, only the owner of the organization has the power to remove members.
        //The subscription seats need to be refunded, and then available for others to use.

        LocalDateTime now = gridClock.now();

        OrganizationDto activeOrganization = getActiveOrganization(requestingRootAccountId);

        OrganizationSubscriptionEntity subscription = reserveSeatForUpdate(activeOrganization.getId());
        validateSubscriptionFound(subscription);
        validateSubscriptionOwnedByRootAccount(subscription, requestingRootAccountId);

        OrganizationMemberEntity membership = organizationMemberRepository.findByOrganizationIdAndId(activeOrganization.getId(), memberId);
        validateMembershipFound(activeOrganization.getDomainName(), membership);

        //okay, the member is real, we've got a lock on the subscription, lets cancel it

        OrganizationSubscriptionSeatEntity subscriptionSeat = organizationSubscriptionSeatRepository.selectValidSubscriptionSeatForUpdate(activeOrganization.getId(), membership.getRootAccountId());

        subscriptionSeat.setCancelDate(now);
        subscriptionSeat.setSubscriptionStatus(SubscriptionStatus.CANCELED);

        organizationSubscriptionSeatRepository.save(subscriptionSeat);

        subscription.setSeatsRemaining(subscription.getSeatsRemaining() + 1);

        organizationSubscriptionRepository.save(subscription);

        //now we need to tombstone the organization member, and all the team memberships for this user.
        //the tombstones include the details of the attached user, at the time of the boot.
        // The connection to this users root user account, will be destroyed.

        MemberDetailsEntity memberDetails = memberDetailsService.lookupMemberDetails(membership.getId());

        OrganizationMemberTombstoneEntity memberTombstone = new OrganizationMemberTombstoneEntity();
        memberTombstone.setId(UUID.randomUUID());
        memberTombstone.setMemberId(membership.getId());
        memberTombstone.setOrganizationId(membership.getOrganizationId());
        memberTombstone.setJoinDate(subscriptionSeat.getActivationDate());
        memberTombstone.setRipDate(now);
        memberTombstone.setDisplayName(memberDetails.getDisplayName());
        memberTombstone.setEmail(memberDetails.getEmail());
        memberTombstone.setUsername(memberDetails.getUsername());
        memberTombstone.setFullName(memberDetails.getFullName());

        organizationMemberTombstoneRepository.save(memberTombstone);

        //any teams the member was a part of, also need removal and tombstones

        teamCapability.removeMemberFromAllTeams(now, membership.getOrganizationId(), membership.getId());

        //now finally, can delete the membership entity

        organizationMemberRepository.delete(membership);

        return new SimpleStatusDto(Status.SUCCESS, "Membership successfully deleted. Organization subscription seat reclaimed.");
    }


    private void validateMembershipFound(String domainName, OrganizationMemberEntity membership) {
        if (membership == null) {
            throw new BadRequestException(ValidationErrorCodes.MEMBER_NOT_FOUND, "No member found in organization " + domainName);
        }
    }


    private void validateSubscriptionFound(OrganizationSubscriptionEntity subscription) {
        if (subscription == null) {
            throw new BadRequestException(ValidationErrorCodes.NO_SUBSCRIPTION_FOUND, "No subscription found for organization");
        }
    }

    private void validateSubscriptionOwnedByRootAccount(OrganizationSubscriptionEntity subscription, UUID rootAccountOwnerId) {
        if (!subscription.getRootAccountOwnerId().equals(rootAccountOwnerId)) {
            throw new BadRequestException(ValidationErrorCodes.MUST_BE_ORGANIZATION_OWNER, "Must be organization subscription owner.");
        }
    }

    public MemberDetailsDto getMemberOfActiveOrganization(UUID rootAccountId, UUID memberId) {
        return null;
    }


    public void validateMemberWithinOrgByMemberId(UUID organizationId, UUID memberId) {
        if (memberId == null) {
            throw new BadRequestException(ValidationErrorCodes.NO_ORG_MEMBERSHIP_FOR_ACCOUNT, "Membership not found in organization");
        }

        OrganizationMemberEntity otherMember = organizationMemberRepository.findById(memberId);
        if (otherMember == null || !otherMember.getOrganizationId().equals(organizationId)) {
            throw new BadRequestException(ValidationErrorCodes.NO_ORG_MEMBERSHIP_FOR_ACCOUNT, "Membership not found in organization");
        }
    }

    public void validateMemberWithinOrg(UUID organizationId, UUID rootAccountId) {
        OrganizationMemberEntity membership = organizationMemberRepository.findByOrganizationIdAndRootAccountId(organizationId, rootAccountId);

        if (membership == null || !membership.getOrganizationId().equals(organizationId)) {
            throw new BadRequestException(ValidationErrorCodes.NO_ORG_MEMBERSHIP_FOR_ACCOUNT, "Membership not found in organization");
        }
    }


    public UUID getMemberIdForUser(UUID organizationId, String userName) {
        OrganizationMemberEntity membership = organizationMemberRepository.findByOrganizationIdAndUsername(organizationId, userName);

        UUID memberId = null;
        if (membership != null) {
            return membership.getId();
        }

        return null;
    }

    public String getUsernameForMemberId(UUID memberId) {
        OrganizationMemberEntity membership = organizationMemberRepository.findById(memberId);

        String userName = null;
        if (membership != null) {
            return membership.getUsername();
        }

        return userName;
    }


    public SimpleStatusDto updateJiraConfiguration(UUID rootAccountId, JiraConfigDto jiraConfigDto) {
        OrganizationMemberEntity activeMembership = getActiveMembership(rootAccountId);

        OrganizationEntity organization = organizationRepository.findById(activeMembership.getOrganizationId());

        OrganizationSubscriptionEntity subscription = organizationSubscriptionRepository.findByOrganizationId(organization.getId());
        validateSubscriptionFound(subscription);

        validateSubscriptionOwnedByRootAccount(subscription, rootAccountId);

        organization.setJiraSiteUrl(jiraConfigDto.getJiraSiteUrl());
        organization.setJiraUser(jiraConfigDto.getJiraUser());
        organization.setJiraApiKey(jiraConfigDto.getJiraApiKey());

        ConnectionResultDto jiraConnection = jiraCapability.validateJiraConnection(organization);

        if (jiraConnection.getStatus() == Status.VALID) {
            organizationRepository.save(organization);
        } else {
            throw new BadRequestException(ValidationErrorCodes.JIRA_CONFIGURATION_INVALID, "Jira configuration invalid, did not save.");
        }

        return new SimpleStatusDto(Status.VALID, "Jira configuration updated.");
    }

    public JiraConfigDto getJiraConfiguration(UUID rootAccountId) {

        OrganizationMemberEntity activeMembership = getActiveMembership(rootAccountId);

        OrganizationEntity organization = organizationRepository.findById(activeMembership.getOrganizationId());

        OrganizationSubscriptionEntity subscription = organizationSubscriptionRepository.findByOrganizationId(organization.getId());
        validateSubscriptionFound(subscription);

        validateSubscriptionOwnedByRootAccount(subscription, rootAccountId);

        return new JiraConfigDto(organization.getJiraSiteUrl(), organization.getJiraUser(), organization.getJiraApiKey());
    }


    public MemberRegistrationDetailsDto joinOrganization(UUID rootAccountId, UUID organizationId, MembershipInputDto membershipInputDto) {
        return null;
    }


    public List<MemberDetailsDto> getOrganizationMembers(UUID rootAccountId, UUID organizationId) {
        return null;
    }


    public OrganizationSubscriptionDto cancelSubscription(UUID rootAccountId, UUID subscriptionId) {
        return null;
    }


    public OrganizationSubscriptionDto getOrganizationSubscription(UUID rootAccountId, UUID subscriptionId) {
        return null;
    }



}
