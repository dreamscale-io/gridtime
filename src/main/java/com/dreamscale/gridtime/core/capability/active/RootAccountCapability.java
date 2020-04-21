package com.dreamscale.gridtime.core.capability.active;

import com.dreamscale.gridtime.api.account.*;
import com.dreamscale.gridtime.api.organization.OnlineStatus;
import com.dreamscale.gridtime.api.status.Status;
import com.dreamscale.gridtime.api.team.TeamDto;
import com.dreamscale.gridtime.core.capability.directory.OrganizationMembershipCapability;
import com.dreamscale.gridtime.core.capability.directory.TeamMembershipCapability;
import com.dreamscale.gridtime.core.capability.integration.EmailCapability;
import com.dreamscale.gridtime.core.capability.operator.GridTalkRouter;
import com.dreamscale.gridtime.core.capability.operator.LearningCircuitOperator;
import com.dreamscale.gridtime.core.domain.active.ActiveAccountStatusEntity;
import com.dreamscale.gridtime.core.domain.active.ActiveAccountStatusRepository;
import com.dreamscale.gridtime.core.domain.circuit.*;
import com.dreamscale.gridtime.core.domain.member.*;
import com.dreamscale.gridtime.core.exception.ConflictErrorCodes;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.commons.JSONTransformer;
import com.dreamscale.gridtime.core.security.RootAccountIdResolver;
import com.dreamscale.gridtime.core.service.GridClock;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.dreamscale.exception.ConflictException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class RootAccountCapability implements RootAccountIdResolver {

    @Autowired
    private RootAccountRepository rootAccountRepository;

    @Autowired
    private ActiveAccountStatusRepository accountStatusRepository;

    @Autowired
    private LearningCircuitOperator learningCircuitOperator;

    @Autowired
    private ActiveWorkStatusManager activeWorkStatusManager;

    @Autowired
    private OrganizationMembershipCapability organizationMembership;

    @Autowired
    private TeamMembershipCapability teamMembership;

    @Autowired
    private MemberConnectionRepository memberConnectionRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationMemberRepository organizationMemberRepository;

    @Autowired
    private OneTimeTicketRepository oneTimeTicketRepository;

    @Autowired
    private GridClock gridClock;

    @Autowired
    private EmailCapability emailCapability;

//    @Autowired
//    private TalkRoomMemberRepository talkRoomMemberRepository;

    @Autowired
    private TalkRoomRepository talkRoomRepository;

    @Autowired
    private GridTalkRouter talkRouter;

    private static final String PUBLIC_ORG_DOMAIN = "public.dreamscale.io";
    private static final String PUBLIC_ORG_NAME = "Public";


    public SimpleStatusDto registerAccount(RootAccountCredentialsInputDto rootAccountCreationInput) {

        String standardizedEmail = standarizeToLowerCase(rootAccountCreationInput.getEmail());
        RootAccountEntity existingRootAccount = rootAccountRepository.findByRootEmail(standardizedEmail);

        validateNoExistingAccount(standardizedEmail, existingRootAccount);

        LocalDateTime now = gridClock.now();

        RootAccountEntity newAccount = new RootAccountEntity();
        newAccount.setId(UUID.randomUUID());
        newAccount.setRootEmail(standardizedEmail);
        newAccount.setRegistrationDate(now);
        newAccount.setLastUpdated(now);
        newAccount.setEmailValidated(false);

        rootAccountRepository.save(newAccount);

        OneTimeTicketEntity oneTimeActivationTicket = issueOneTimeActivationTicket(now, newAccount.getId());

        emailCapability.sendDownloadAndActivationEmail(standardizedEmail, oneTimeActivationTicket.getTicketCode());

        return new SimpleStatusDto(Status.SUCCESS, "Account Activation email sent.");
    }

    @Transactional
    public AccountActivationDto activate(String activationCode) {
        OneTimeTicketEntity oneTimeTicket = oneTimeTicketRepository.findByTicketCode(activationCode);

        AccountActivationDto accountActivationDto = new AccountActivationDto();

        LocalDateTime now = gridClock.now();

        if (oneTimeTicket == null) {
            accountActivationDto.setMessage("Activation code not found.");
            accountActivationDto.setStatus(Status.FAILED);
        } else if (isExpired(now, oneTimeTicket)) {
            accountActivationDto.setMessage("Activation code is expired.");
            accountActivationDto.setStatus(Status.FAILED);

            oneTimeTicketRepository.delete(oneTimeTicket);

        } else {
            String apiKey = generateAPIKey();

            RootAccountEntity rootAccountEntity = rootAccountRepository.findById(oneTimeTicket.getOwnerId());

            validateAccountExists("activation ticket owner", rootAccountEntity);

            rootAccountEntity.setApiKey(apiKey);
            rootAccountEntity.setActivationDate(now);
            rootAccountEntity.setLastUpdated(now);
            rootAccountEntity.setEmailValidated(true);

            rootAccountRepository.save(rootAccountEntity);

            OrganizationEntity publicOrg = findOrCreatePublicOrg();

            OrganizationMemberEntity pubOrgMembership = organizationMemberRepository.findByOrganizationIdAndRootAccountId(publicOrg.getId(), rootAccountEntity.getId());

            if (pubOrgMembership == null) {
                pubOrgMembership = new OrganizationMemberEntity();
                pubOrgMembership.setId(UUID.randomUUID());
                pubOrgMembership.setOrganizationId(publicOrg.getId());
                pubOrgMembership.setRootAccountId(rootAccountEntity.getId());
                pubOrgMembership.setEmail(rootAccountEntity.getRootEmail());

                organizationMemberRepository.save(pubOrgMembership);
            }

            oneTimeTicketRepository.delete(oneTimeTicket);

            accountActivationDto.setEmail(rootAccountEntity.getRootEmail());
            accountActivationDto.setApiKey(apiKey);
            accountActivationDto.setMessage("Your account has been successfully activated.");
            accountActivationDto.setStatus(Status.SUCCESS);
        }

        return accountActivationDto;
    }

    private boolean isExpired(LocalDateTime now, OneTimeTicketEntity oneTimeTicket) {
        return now.isAfter(oneTimeTicket.getExpirationDate());
    }

    private OneTimeTicketEntity issueOneTimeActivationTicket(LocalDateTime now, UUID ticketOwnerId) {
        OneTimeTicketEntity oneTimeTicket = new OneTimeTicketEntity();
        oneTimeTicket.setId(UUID.randomUUID());
        oneTimeTicket.setOwnerId(ticketOwnerId);
        oneTimeTicket.setTicketType(TicketType.ACTIVATION);
        oneTimeTicket.setTicketCode(generateTicketCode());
        oneTimeTicket.setIssueDate(now);
        oneTimeTicket.setExpirationDate(now.plusDays(1));

        oneTimeTicketRepository.save(oneTimeTicket);
        return oneTimeTicket;
    }

    private OneTimeTicketEntity issueOneTimeEmailValidationTicket(LocalDateTime now, UUID ticketOwnerId, String newEmail) {
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

    public SimpleStatusDto reset(String email) {

        String standardizedEmail = standarizeToLowerCase(email);
        RootAccountEntity existingRootAccount = rootAccountRepository.findByRootEmail(standardizedEmail);

        validateAccountExists(standardizedEmail, existingRootAccount);

        LocalDateTime now = gridClock.now();

        OneTimeTicketEntity oneTimeTicket = issueOneTimeActivationTicket(now, existingRootAccount.getId());

        emailCapability.sendAccountResetEmail(standardizedEmail, oneTimeTicket.getTicketCode());

        return new SimpleStatusDto(Status.SUCCESS, "Account Reset email sent.");

    }

    public AccountActivationDto cycleKeys(UUID rootAccountId) {

        RootAccountEntity rootAccountEntity = rootAccountRepository.findById(rootAccountId);

        LocalDateTime now = gridClock.now();

        String apiKey = generateAPIKey();
        rootAccountEntity.setApiKey(apiKey);
        rootAccountEntity.setLastUpdated(now);

        rootAccountRepository.save(rootAccountEntity);

        AccountActivationDto accountActivationDto = new AccountActivationDto();

        accountActivationDto.setEmail(rootAccountEntity.getRootEmail());
        accountActivationDto.setApiKey(apiKey);
        accountActivationDto.setMessage("Your account keys have been successfully cycled.");
        accountActivationDto.setStatus(Status.SUCCESS);

        return accountActivationDto;

    }

    private OrganizationEntity findOrCreatePublicOrg() {

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

    private void validateNoExistingAccount(String standarizedEmail, RootAccountEntity existingRootAccount) {

        if (existingRootAccount != null) {
            throw new ConflictException(ConflictErrorCodes.EMAIL_ALREADY_IN_USE, "Email already in use: " + standarizedEmail);
        }
    }

    private void validateAccountExists(String searchForAccountCriteria, RootAccountEntity existingRootAccount) {

        if (existingRootAccount == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_ACCOUNT_EMAIL, "Unable to find account using: " + searchForAccountCriteria);
        }
    }

    public ConnectionStatusDto login(UUID rootAccountId) {

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        ActiveAccountStatusEntity accountStatusEntity = findOrCreateActiveAccountStatus(now, rootAccountId);

        accountStatusEntity.setConnectionId(UUID.randomUUID());
        accountStatusEntity.setOnlineStatus(OnlineStatus.Connecting);

        accountStatusRepository.save(accountStatusEntity);

        ConnectionStatusDto statusDto = new ConnectionStatusDto();
        statusDto.setConnectionId(accountStatusEntity.getConnectionId());
        statusDto.setStatus(Status.VALID);
        statusDto.setMessage("Successfully logged in");

        OrganizationMemberEntity membership = organizationMembership.getDefaultMembership(rootAccountId);
        statusDto.setMemberId(membership.getId());
        statusDto.setOrganizationId(membership.getOrganizationId());
        statusDto.setUserName(membership.getUsername());

        TeamDto team = teamMembership.getMyHomeTeam(membership.getOrganizationId(), membership.getId());

        if (team != null) {
            statusDto.setTeamId(team.getId());
        }

        return statusDto;
    }

    public SimpleStatusDto connect(UUID connectionId) {

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        ActiveAccountStatusEntity accountStatusEntity = accountStatusRepository.findByConnectionId(connectionId);

        validateConnectionExists(connectionId, accountStatusEntity);

        //TODO needs to be updated for default org, or whatever org we're logging in with...

        OrganizationMemberEntity membership = organizationMembership.getDefaultMembership(accountStatusEntity.getRootAccountId());

        MemberConnectionEntity memberConnection = memberConnectionRepository.findByConnectionId(connectionId);


        List<TalkRoomEntity> talkRooms = talkRoomRepository.findRoomsByMembership(memberConnection.getOrganizationId(), memberConnection.getMemberId());

        talkRouter.joinAllRooms(connectionId, talkRooms);

        accountStatusEntity.setOnlineStatus(OnlineStatus.Online);
        accountStatusRepository.save(accountStatusEntity);

        activeWorkStatusManager.pushTeamMemberStatusUpdate(membership.getOrganizationId(), membership.getId(), now, nanoTime);

        SimpleStatusDto statusDto = new SimpleStatusDto();
        statusDto.setStatus(Status.VALID);
        statusDto.setMessage("Account connected.");

        return statusDto;
    }

    private List<UUID> getRoomIdsToJoin(List<TalkRoomMemberEntity> roomMemberships) {

        List<UUID> roomIds = new ArrayList<>();

        for (TalkRoomMemberEntity roomMembership : roomMemberships) {
            roomIds.add(roomMembership.getRoomId());
        }

        return roomIds;
    }

    private void validateConnectionExists(UUID connectionId, ActiveAccountStatusEntity accountStatusEntity) {
        if (accountStatusEntity == null) {
            throw new BadRequestException(ValidationErrorCodes.INVALID_CONNECTION, "Unable to find connection " + connectionId);
        }
    }


    public SimpleStatusDto logout(UUID rootAccountId) {

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        ActiveAccountStatusEntity accountStatusEntity = findOrCreateActiveAccountStatus(now, rootAccountId);

        UUID oldConnectionId = accountStatusEntity.getConnectionId();

        accountStatusEntity.setOnlineStatus(OnlineStatus.Offline);
        accountStatusEntity.setConnectionId(null);

        accountStatusRepository.save(accountStatusEntity);

        if (oldConnectionId != null) {
            learningCircuitOperator.notifyRoomsOfMemberDisconnect(oldConnectionId);
        }

        OrganizationMemberEntity membership = organizationMembership.getDefaultMembership(rootAccountId);

        activeWorkStatusManager.pushTeamMemberStatusUpdate(membership.getOrganizationId(), membership.getId(), now, nanoTime);

        return new SimpleStatusDto(Status.SUCCESS, "Successfully logged out");
    }


    public SimpleStatusDto heartbeat(UUID rootAccountId, HeartbeatDto heartbeat) {
        SimpleStatusDto heartBeatStatus;

        LocalDateTime now = gridClock.now();

        ActiveAccountStatusEntity accountStatusEntity = findOrCreateActiveAccountStatus(now, rootAccountId);

        if (isNotOnline(accountStatusEntity)) {
            heartBeatStatus = new SimpleStatusDto(Status.FAILED, "Please login before updating heartbeat");
        } else {
            accountStatusEntity.setLastActivity(now);
            accountStatusEntity.setDeltaTime(heartbeat.getDeltaTime());
            accountStatusRepository.save(accountStatusEntity);

            heartBeatStatus = new SimpleStatusDto(Status.SUCCESS, "beat.");
        }

        return heartBeatStatus;
    }

    private boolean isNotOnline(ActiveAccountStatusEntity accountStatusEntity) {
        return (accountStatusEntity.getOnlineStatus() == null
                || accountStatusEntity.getOnlineStatus() != OnlineStatus.Online);
    }

    private ActiveAccountStatusEntity findOrCreateActiveAccountStatus(LocalDateTime now, UUID rootAccountId) {

        ActiveAccountStatusEntity accountStatusEntity = accountStatusRepository.findByRootAccountId(rootAccountId);
        if (accountStatusEntity == null) {
            accountStatusEntity = new ActiveAccountStatusEntity();
            accountStatusEntity.setRootAccountId(rootAccountId);
            accountStatusEntity.setLastActivity(now);

        } else {
            accountStatusEntity.setLastActivity(now);
        }

        return accountStatusEntity;
    }

    public UUID findAccountIdByApiKey(String apiKey) {
        UUID rootAccountId = null;

        RootAccountEntity rootAccountEntity = rootAccountRepository.findByApiKey(apiKey);
        if (rootAccountEntity != null) {
            rootAccountId = rootAccountEntity.getId();
        }
        return rootAccountId;
    }

    public LocalDateTime getActivationDate(UUID rootAccountId) {
        RootAccountEntity rootAccountEntity = rootAccountRepository.findById(rootAccountId);
        return rootAccountEntity.getActivationDate();
    }

    @Override
    public UUID findAccountIdByConnectionId(String connectionId) {
        UUID rootAccountId = null;
        UUID connectUuid = UUID.fromString(connectionId);
        ActiveAccountStatusEntity accountStatusEntity = accountStatusRepository.findByConnectionId(connectUuid);
        if (accountStatusEntity != null) {
            rootAccountId = accountStatusEntity.getRootAccountId();
        }
        return rootAccountId;
    }

    private String generateAPIKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String generateTicketCode() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public UserProfileDto getProfile(UUID rootAccountId) {

        RootAccountEntity rootAccountEntity = rootAccountRepository.findById(rootAccountId);

        return toDto(rootAccountEntity);
    }

    private UserProfileDto toDto(RootAccountEntity rootAccount) {
        UserProfileDto userProfileDto = new UserProfileDto();
        userProfileDto.setRootId(rootAccount.getId());
        userProfileDto.setFullName(rootAccount.getFullName());
        userProfileDto.setDisplayName(rootAccount.getDisplayName());
        userProfileDto.setEmail(rootAccount.getRootEmail());
        userProfileDto.setUserName(rootAccount.getRootUsername());

        return userProfileDto;
    }

    public UserProfileDto updateProfileUserName(UUID rootAccountId, String username) {

        RootAccountEntity rootAccountEntity = rootAccountRepository.findById(rootAccountId);

        String oldUserName = rootAccountEntity.getRootUsername();

        rootAccountEntity.setRootUsername(username);
        rootAccountEntity.setLowerCaseRootUserName(standarizeToLowerCase(username));
        rootAccountEntity.setLastUpdated(gridClock.now());

        try {
            rootAccountRepository.save(rootAccountEntity);
        } catch (Exception ex) {
            String conflictMsg = "Conflict in changing account username from "+oldUserName+" to "+ username;
            log.warn(conflictMsg);

            throw new ConflictException(ConflictErrorCodes.CONFLICTING_USER_NAME, conflictMsg);
        }

        return toDto(rootAccountEntity);
    }

    public UserProfileDto updateProfileEmail(UUID rootAccountId, String newEmail) {
        RootAccountEntity rootAccountEntity = rootAccountRepository.findById(rootAccountId);

        LocalDateTime now = gridClock.now();

        String standardizedEmail = standarizeToLowerCase(newEmail);

        OneTimeTicketEntity oneTimeTicket = issueOneTimeEmailValidationTicket(now, rootAccountId, standardizedEmail);

        emailCapability.sendEmailValidationEmail(standardizedEmail, oneTimeTicket.getTicketCode());

        rootAccountRepository.save(rootAccountEntity);

        UserProfileDto profile = toDto(rootAccountEntity);

        profile.setEmail(newEmail + " (pending validation)");

        return profile;
    }

    public SimpleStatusDto validateProfileEmail(String validationCode) {

        OneTimeTicketEntity oneTimeTicket = oneTimeTicketRepository.findByTicketCode(validationCode);

        SimpleStatusDto simpleStatus = new SimpleStatusDto();

        LocalDateTime now = gridClock.now();

        if (oneTimeTicket == null) {
            simpleStatus.setMessage("Validation code not found.");
            simpleStatus.setStatus(Status.FAILED);
        } else if (isExpired(now, oneTimeTicket)) {
            simpleStatus.setMessage("Validation code is expired.");
            simpleStatus.setStatus(Status.FAILED);

            oneTimeTicketRepository.delete(oneTimeTicket);

        } else {

            RootAccountEntity rootAccountEntity = rootAccountRepository.findById(oneTimeTicket.getOwnerId());

            validateAccountExists("validation ticket owner", rootAccountEntity);

            String email = oneTimeTicket.getEmailProp();

            rootAccountEntity.setRootEmail(email);
            rootAccountEntity.setLastUpdated(now);
            rootAccountEntity.setEmailValidated(true);

            rootAccountRepository.save(rootAccountEntity);

            oneTimeTicketRepository.delete(oneTimeTicket);

            simpleStatus.setMessage("Profile Email successfully updated.");
            simpleStatus.setStatus(Status.SUCCESS);
        }

        return simpleStatus;
    }


    public UserProfileDto updateProfileFullName(UUID rootAccountId, String fullName) {

        RootAccountEntity rootAccountEntity = rootAccountRepository.findById(rootAccountId);

        rootAccountEntity.setFullName(fullName);
        rootAccountEntity.setLastUpdated(gridClock.now());

        rootAccountRepository.save(rootAccountEntity);

        return toDto(rootAccountEntity);
    }

    public UserProfileDto updateProfileDisplayName(UUID rootAccountId, String displayName) {
        RootAccountEntity rootAccountEntity = rootAccountRepository.findById(rootAccountId);

        rootAccountEntity.setDisplayName(displayName);
        rootAccountEntity.setLastUpdated(gridClock.now());

        rootAccountRepository.save(rootAccountEntity);

        return toDto(rootAccountEntity);
    }

    private String standarizeToLowerCase(String name) {
        return name.toLowerCase();
    }



}
