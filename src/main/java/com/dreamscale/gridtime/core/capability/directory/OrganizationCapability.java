package com.dreamscale.gridtime.core.capability.directory;

import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.organization.*;
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
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.dreamscale.exception.ConflictException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private TeamMembershipCapability teamMembershipCapability;


    @Autowired
    private OrganizationSubscriptionRepository organizationSubscriptionRepository;

    @Autowired
    private OrganizationSubscriptionDetailsRepository organizationSubscriptionDetailsRepository;

    @Autowired
    private JiraCapability jiraCapability;

    @Autowired
    private EmailCapability emailCapability;

    @Autowired
    private OneTimeTicketCapability oneTimeTicketCapability;

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

        teamMembershipCapability.createEveryoneTeam(organizationEntity.getId());

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
            createOrgMembership(subscription, rootAccountId, standardizedEmail);

            statusDto.setStatus(Status.JOINED);
            statusDto.setMessage("Member added to organization.");
        } else {

            OneTimeTicketEntity oneTimeTicket = oneTimeTicketCapability.issueOneTimeOrgEmailValidationTicket(now, rootAccountId, org.getId(), standardizedEmail);
            //need to valid
            return emailCapability.sendEmailToValidateOrgEmailAddress(standardizedEmail, oneTimeTicket.getTicketCode());

        }
        return statusDto;
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

            OrganizationSubscriptionEntity subscription = organizationSubscriptionRepository.findByOrganizationId(organizationId);

            createOrgMembership(subscription, rootAccountId, orgEmail);

            simpleStatusDto.setStatus(Status.JOINED);
            simpleStatusDto.setMessage("Member added to organization.");

            oneTimeTicketCapability.delete(oneTimeTicket);
        }

        return simpleStatusDto;
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








    private OrganizationMemberEntity createOrgMembership(OrganizationSubscriptionEntity subscription, UUID rootAccountId, String email) {

        UUID organizationId = subscription.getOrganizationId();
        validateAvailableSeat(subscription);

        OrganizationMemberEntity membership = organizationMemberRepository.findByOrganizationIdAndRootAccountId(organizationId, rootAccountId);

        if (membership == null) {

            subscription.setSeatsRemaining( subscription.getSeatsRemaining() - 1);
            organizationSubscriptionRepository.save(subscription);

            membership = new OrganizationMemberEntity();
            membership.setId(UUID.randomUUID());
            membership.setOrganizationId(organizationId);
            membership.setRootAccountId(rootAccountId);
            membership.setEmail(email);

            organizationMemberRepository.save(membership);

            teamMembershipCapability.addMemberToEveryone(organizationId, membership.getId());
        }

        return membership;

    }

    private void validateAvailableSeat(OrganizationSubscriptionEntity subscription) {
        if (subscription.getSeatsRemaining() <= 0) {
            throw new ConflictException(ConflictErrorCodes.NO_SEATS_AVAILABLE, "No seats available, check with your admin about increasing capacity.");
        }
    }

    private void validateEmailWithinDomain(String standardizedEmail, String domainName) {
        if ( ! standardizedEmail.endsWith(domainName) ) {
            throw new BadRequestException(ValidationErrorCodes.EMAIL_NOT_IN_DOMAIN, "Email '"+standardizedEmail+"' must be in the domain.");
        }
    }

    private boolean requiresEmailMatch(OrganizationSubscriptionEntity subscription) {
        return subscription.getRequireMemberEmailInDomain();
    }

    private void validateTokenFound(LocalDateTime now, String inviteToken, OrganizationInviteTokenEntity tokenEntity) {
        if (tokenEntity == null || isExpired(now, tokenEntity)) {
            throw new BadRequestException(ValidationErrorCodes.INVALID_OR_EXPIRED_INVITE_TOKEN, "Invite token '"+inviteToken+"' is expired or not found.");
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
            throw new BadRequestException(ValidationErrorCodes.MISSING_FIELD, "field '"+fieldName + "' is required.");
        }
    }

    private void validateDomainNotAlreadyRegistered(String domainName) {
        String standardizedDomain = domainName.toLowerCase();

        OrganizationEntity existingOrg = organizationRepository.findByDomainName(standardizedDomain);

        if (existingOrg != null) {
            throw new ConflictException(ConflictErrorCodes.ORG_DOMAIN_ALREADY_IN_USE, "Org domain '"+domainName + "' already in use.");
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

        for (OrganizationEntity org : orgs ) {
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

        if (activeMembership == null ) {
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

        //TODO refactor this to query for organizations directly via membership, below is lazy

        List<OrganizationMemberEntity> orgMemberships = memberRepository.findByRootAccountId(rootAccountId);

        List<OrganizationDto> participatingOrgs = new ArrayList<>();

        for (OrganizationMemberEntity membership : orgMemberships) {
            OrganizationEntity organizationEntity = organizationRepository.findById(membership.getOrganizationId());
            participatingOrgs.add(orgOutputMapper.toApi(organizationEntity));
        }

        return participatingOrgs;
    }

//    public OrganizationDto getDefaultOrganizationWithInvitation(UUID rootAccountId) {
//        List<OrganizationMemberEntity> orgMemberships = memberRepository.findByRootAccountId(rootAccountId);
//
//        if (orgMemberships == null || orgMemberships.size() == 0) {
//            throw new BadRequestException(ValidationErrorCodes.NO_ORG_MEMBERSHIP_FOR_ACCOUNT, "organization membership not found");
//        }
//
//        OrganizationEntity organizationEntity = organizationRepository.findById(orgMemberships.get(0).getOrganizationId());
//
//        OrganizationInviteTokenEntity inviteToken = inviteTokenRepository.findByOrganizationId(organizationEntity.getId());
//
//        OrganizationDto outputOrg = orgOutputMapper.toApi(organizationEntity);
//        outputOrg.setConnectionStatus(Status.VALID);
//        outputOrg.setInviteLink(constructInvitationLink(inviteToken.getToken()));
//        outputOrg.setInviteToken(inviteToken.getToken());
//
//        return outputOrg;
//    }

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
        return null;
    }





    public MemberRegistrationDetailsDto joinOrganization(UUID rootAccountId, UUID organizationId, MembershipInputDto membershipInputDto) {
        return null;
    }

    public OrganizationDto getMyActiveOrganization(UUID rootAccountId) {
        return null;
    }


    public List<MemberRegistrationDto> getOrganizationMembers(UUID rootAccountId, UUID organizationId) {
        return null;
    }


    public SimpleStatusDto removeOrganizationMember(UUID rootAccountId, UUID memberId) {
        return null;
    }





    public OrganizationSubscriptionDto cancelSubscription(UUID rootAccountId, UUID subscriptionId) {
        return null;
    }

    public List<MemberRegistrationDto> getMembersOfActiveOrganization(UUID rootAccountId) {
        return null;
    }

    public MemberRegistrationDto getMemberOfActiveOrganization(UUID rootAccountId, UUID memberId) {
        return null;
    }

    public JiraConfigDto getJiraConfiguration(UUID rootAccountId) {
        return null;
    }

    public OrganizationSubscriptionDto getOrganizationSubscription(UUID rootAccountId, UUID subscriptionId) {
        return null;
    }


}
