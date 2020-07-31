package com.dreamscale.gridtime.core.capability.membership;

import com.dreamscale.gridtime.api.account.*;
import com.dreamscale.gridtime.api.organization.OnlineStatus;
import com.dreamscale.gridtime.api.organization.OrganizationDto;
import com.dreamscale.gridtime.api.status.Status;
import com.dreamscale.gridtime.api.team.TeamDto;
import com.dreamscale.gridtime.api.team.TeamLinkDto;
import com.dreamscale.gridtime.core.capability.active.ActiveWorkStatusManager;
import com.dreamscale.gridtime.core.capability.circuit.GridTalkRouter;
import com.dreamscale.gridtime.core.capability.circuit.RoomOperator;
import com.dreamscale.gridtime.core.capability.external.EmailCapability;
import com.dreamscale.gridtime.core.capability.journal.JournalCapability;
import com.dreamscale.gridtime.core.domain.active.ActiveAccountStatusEntity;
import com.dreamscale.gridtime.core.domain.active.ActiveAccountStatusRepository;
import com.dreamscale.gridtime.core.domain.circuit.MemberConnectionEntity;
import com.dreamscale.gridtime.core.domain.circuit.*;
import com.dreamscale.gridtime.core.domain.member.*;
import com.dreamscale.gridtime.core.exception.ConflictErrorCodes;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.security.RootAccountIdResolver;
import com.dreamscale.gridtime.core.capability.system.GridClock;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.dreamscale.exception.BadRequestException;
import org.dreamscale.exception.ConflictException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class RootAccountCapability implements RootAccountIdResolver {

    @Autowired
    private RootAccountRepository rootAccountRepository;

    @Autowired
    private ActiveAccountStatusRepository accountStatusRepository;

    @Autowired
    private ActiveWorkStatusManager activeWorkStatusManager;

    @Autowired
    private OrganizationCapability organizationCapability;

    @Autowired
    private TeamCapability teamMembership;

    @Autowired
    private MemberConnectionRepository memberConnectionRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationMemberRepository organizationMemberRepository;

    @Autowired
    private OneTimeTicketCapability oneTimeTicketCapability;

    @Autowired
    private GridClock gridClock;

    @Autowired
    private GridTalkRouter talkRouter;

    @Autowired
    private EmailCapability emailCapability;

    @Autowired
    private InviteCapability inviteCapability;

    @Autowired
    private TeamCapability teamCapability;

    @Autowired
    private JournalCapability journalCapability;

    @Autowired
    private RoomOperator roomOperator;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private RootAccountTombstoneRepository rootAccountTombstoneRepository;

    @Value( "${torchie.public.org.domain}" )
    private String publicTorchieDomain;

    @Value("${account.idle.threshold.minutes}")
    private int MINUTES_WITHOUT_HEARTBEAT_TO_IDLE;

    @Value("${account.disconnect.threshold.minutes}")
    private int MINUTES_WITHOUT_HEARTBEAT_TO_DISCONNECT;


    @Transactional
    public UserProfileDto registerAccount(RootAccountCredentialsInputDto rootAccountCreationInput) {

        LocalDateTime now = gridClock.now();
        String standardizedEmail = standarizeToLowerCase(rootAccountCreationInput.getEmail());
        String password = rootAccountCreationInput.getPassword();

        RootAccountEntity newAccount = createAccountAndFlush(now, standardizedEmail, rootAccountCreationInput);

        updatePassword(newAccount.getId(), password);

        OneTimeTicketEntity oneTimeActivationTicket = oneTimeTicketCapability.issueOneTimeActivationTicket(now, newAccount.getId());
        emailCapability.sendDownloadAndActivationEmail(standardizedEmail, oneTimeActivationTicket.getTicketCode());

        return toDto(newAccount, null);
    }

    @Transactional
    public AccountActivationDto activate(String activationCode) {
        OneTimeTicketEntity oneTimeTicket = oneTimeTicketCapability.findByTicketCode(activationCode);

        AccountActivationDto accountActivationDto = new AccountActivationDto();

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        if (oneTimeTicket == null) {
            accountActivationDto.setMessage("Activation code not found.");
            accountActivationDto.setStatus(Status.FAILED);
        } else if (oneTimeTicketCapability.isExpired(now, oneTimeTicket)) {
            accountActivationDto.setMessage("Activation code is expired.");
            accountActivationDto.setStatus(Status.FAILED);

            oneTimeTicketCapability.expire(now, oneTimeTicket);

        } else {

            String apiKey = generateAPIKey();

            //if this was an invite ticket, the owner will be someone else, so process these cases differently...

            RootAccountEntity rootAccountEntity = null;

            if (oneTimeTicket.getTicketType() == TicketType.ACTIVATION_BY_OWNER) {

                rootAccountEntity = rootAccountRepository.findById(oneTimeTicket.getOwnerId());

                validateAccountExists("activation by ticket owner", rootAccountEntity);

                rootAccountEntity.setApiKey(apiKey);
                rootAccountEntity.setActivationDate(now);
                rootAccountEntity.setLastUpdated(now);
                rootAccountEntity.setEmailValidated(true);

                rootAccountRepository.save(rootAccountEntity);


            } else if (TicketType.isInviteAndActivateType(oneTimeTicket.getTicketType())) {

                //this person will be totally new to the system, so make a new root account

                String invitedEmail = oneTimeTicket.getEmailProp();

                rootAccountEntity = new RootAccountEntity();
                rootAccountEntity.setId(UUID.randomUUID());
                rootAccountEntity.setRootEmail(invitedEmail);
                rootAccountEntity.setApiKey(apiKey);
                rootAccountEntity.setRegistrationDate(now);
                rootAccountEntity.setActivationDate(now);
                rootAccountEntity.setEmailValidated(true);

                String generatedUsername = extractBaseUserFromEmail(invitedEmail);

                rootAccountEntity.setRootUsername(generatedUsername);
                rootAccountEntity.setLowercaseRootUsername(generatedUsername);

                rootAccountEntity = tryToSaveAndReserveUsername(rootAccountEntity);
                rootAccountEntity.setDisplayName(rootAccountEntity.getRootUsername());
                rootAccountEntity.setFullName(rootAccountEntity.getRootUsername());

                rootAccountRepository.save(rootAccountEntity);

            } else {
                throw new BadRequestException(ValidationErrorCodes.INVALID_TICKET_TYPE, "Invalid ticket type for activation.");
            }

            OrganizationEntity publicOrg = organizationCapability.findOrCreatePublicOrg(now);

            OrganizationMemberEntity pubOrgMembership = organizationMemberRepository.findByOrganizationIdAndRootAccountId(publicOrg.getId(), rootAccountEntity.getId());

            if (pubOrgMembership == null) {
                pubOrgMembership = new OrganizationMemberEntity();
                pubOrgMembership.setId(UUID.randomUUID());
                pubOrgMembership.setOrganizationId(publicOrg.getId());
                pubOrgMembership.setRootAccountId(rootAccountEntity.getId());
                pubOrgMembership.setEmail(rootAccountEntity.getRootEmail());
                pubOrgMembership.setUsername(rootAccountEntity.getRootUsername());
                pubOrgMembership.setLowercaseUsername(rootAccountEntity.getLowercaseRootUsername());

                organizationMemberRepository.save(pubOrgMembership);

                teamCapability.createMeTeam(now, publicOrg.getId(), pubOrgMembership.getId());

            }

            inviteCapability.useInvitationKey(now, rootAccountEntity.getId(), activationCode);

            writeJournalWelcomeMessage(now, nanoTime, rootAccountEntity.getId(), oneTimeTicket);

            accountActivationDto.setEmail(rootAccountEntity.getRootEmail());
            accountActivationDto.setUsername(rootAccountEntity.getRootUsername());
            accountActivationDto.setApiKey(apiKey);
            accountActivationDto.setMessage("Your account has been successfully activated.");
            accountActivationDto.setStatus(Status.VALID);
        }

        return accountActivationDto;
    }

    private void writeJournalWelcomeMessage(LocalDateTime now, Long nanoTime, UUID rootAccountId, OneTimeTicketEntity inviteTicket) {

        String welcomeMessage = "";

        UUID organizationId = inviteTicket.getOrganizationIdProp();
        UUID teamId = inviteTicket.getTeamIdProp();

        if (inviteTicket.getTicketType().equals(TicketType.ACTIVATION_BY_OWNER)) {
            welcomeMessage = "Welcome to the " + publicTorchieDomain + " hyperspace.";
        }

        if (inviteTicket.getTicketType().equals(TicketType.ACTIVATE_AND_INVITE_TO_ORG)) {

            OrganizationEntity organization = organizationRepository.findById(organizationId);
            OrganizationMemberEntity senderOfTicket = organizationCapability.findMember(organizationId, inviteTicket.getOwnerId());

            welcomeMessage = "Welcome to the "+organization.getDomainName() + " hyperspace. You've been invited by @"+senderOfTicket.getUsername();
        }

        if (inviteTicket.getTicketType().equals(TicketType.ACTIVATE_AND_INVITE_TO_ORG_AND_TEAM)) {

            OrganizationEntity organization = organizationRepository.findById(organizationId);
            OrganizationMemberEntity senderOfTicket = organizationCapability.findMember(organizationId, inviteTicket.getOwnerId());
            TeamLinkDto teamLinkDto = teamCapability.getTeamLink(organizationId, teamId);

            welcomeMessage = "Welcome to the " + organization.getDomainName() + " hyperspace. " +
                    "You've been invited to team "+teamLinkDto.getName() + " by @"+senderOfTicket.getUsername();
        }

        OrganizationMemberEntity myMembership = organizationCapability.getActiveMembership(rootAccountId);

        log.info(welcomeMessage);

        journalCapability.createWelcomeMessage(now, nanoTime, myMembership.getOrganizationId(), myMembership.getId(), welcomeMessage);
    }

    @Transactional
    public SimpleStatusDto delete(String activationCode) {

        LocalDateTime now = gridClock.now();

        TicketTombstoneEntity ticketTombstone = oneTimeTicketCapability.findGraveyardTicket(activationCode);

        if (ticketTombstone != null && !oneTimeTicketCapability.isExpired(now, ticketTombstone)) {

            UUID usedBy = ticketTombstone.getUsedBy();

            RootAccountEntity rootAccount = rootAccountRepository.findById(usedBy);

            if (rootAccount != null) {
                List<OrganizationMemberEntity> memberships = organizationMemberRepository.findByRootAccountId(rootAccount.getId());

                for (OrganizationMemberEntity membership : memberships) {
                    organizationCapability.removeMemberFromOrg(now, membership.getOrganizationId(), membership.getId());
                }

                convertToTombstoneAndDelete(now, rootAccount);

                return new SimpleStatusDto(Status.DELETED, "Removed account mapped to activation code.");
            }
        }

        return new SimpleStatusDto(Status.NO_ACTION, "Unable to delete account.");

    }

    private void convertToTombstoneAndDelete(LocalDateTime now, RootAccountEntity rootAccount) {

        RootAccountTombstoneEntity rootAccountTombstone = new RootAccountTombstoneEntity();
        rootAccountTombstone.setId(UUID.randomUUID());
        rootAccountTombstone.setRootAccountId(rootAccount.getId());

        rootAccountTombstone.setActivationDate(rootAccount.getActivationDate());
        rootAccountTombstone.setRegistrationDate(rootAccount.getRegistrationDate());
        rootAccountTombstone.setApiKey(rootAccount.getApiKey());

        rootAccountTombstone.setRootUsername(rootAccount.getRootUsername());
        rootAccountTombstone.setLowercaseRootUsername(rootAccount.getLowercaseRootUsername());
        rootAccountTombstone.setEmailValidated(rootAccount.isEmailValidated());
        rootAccountTombstone.setRootEmail(rootAccount.getRootEmail());
        rootAccountTombstone.setDisplayName(rootAccount.getDisplayName());
        rootAccountTombstone.setFullName(rootAccount.getFullName());

        rootAccountTombstone.setRipDate(now);

        rootAccountTombstoneRepository.save(rootAccountTombstone);

        rootAccountRepository.delete(rootAccount);
    }

    public SimpleStatusDto reset(String email) {

        String standardizedEmail = standarizeToLowerCase(email);
        RootAccountEntity existingRootAccount = rootAccountRepository.findByRootEmail(standardizedEmail);

        validateAccountExists(standardizedEmail, existingRootAccount);

        LocalDateTime now = gridClock.now();

        OneTimeTicketEntity oneTimeTicket = oneTimeTicketCapability.issueOneTimeActivationTicket(now, existingRootAccount.getId());

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
        accountActivationDto.setUsername(rootAccountEntity.getRootUsername());
        accountActivationDto.setApiKey(apiKey);
        accountActivationDto.setMessage("Your account keys have been successfully cycled.");
        accountActivationDto.setStatus(Status.SUCCESS);

        return accountActivationDto;
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

    private void validateUserOrPasswordMatch(String searchForAccountCriteria, RootAccountEntity foundIfPasswordMatches) {

        if (foundIfPasswordMatches == null) {
            throw new BadRequestException(ValidationErrorCodes.INVALID_USER_OR_PASSWORD, "Invalid user or password for: " + searchForAccountCriteria);
        }
    }

    @Transactional
    public ConnectionStatusDto login(UUID rootAccountId) {
        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        ActiveAccountStatusEntity accountStatus = findOrCreateActiveAccountStatus(now, rootAccountId);

        ConnectionStatusDto connectionStatus = null;

        if (accountStatus.getOnlineStatus() != null && accountStatus.getOnlineStatus().equals(OnlineStatus.Idle)) {
            accountStatus.setLastActivity(now);
            accountStatus.setConnectionId(UUID.randomUUID());
            accountStatus.setOnlineStatus(OnlineStatus.Online);

            accountStatusRepository.save(accountStatus);

            entityManager.flush();

            OrganizationMemberEntity activeMembership = organizationCapability.findMember(accountStatus.getLoggedInOrganizationId(), rootAccountId);

            connectionStatus = createValidConnectionStatusDto(activeMembership, accountStatus);

            MemberConnectionEntity connection = memberConnectionRepository.findByConnectionId(accountStatus.getConnectionId());

            activeWorkStatusManager.pushTeamMemberStatusUpdate(connection.getOrganizationId(), connection.getMemberId(), now, nanoTime);

            roomOperator.updateStatusInAllRooms(now, nanoTime, connection);

        } else {
            OrganizationMemberEntity membership = organizationCapability.getDefaultOrganizationMembership(rootAccountId);

            connectionStatus = loginAsOrganizationMember(now, rootAccountId, membership);
        }

        return connectionStatus;

    }

    public ConnectionStatusDto loginWithPassword(String username, String password) {
        LocalDateTime now = gridClock.now();

        RootAccountEntity rootAccount = loginAndFindAccountWithUserPassword(username, password);

        OrganizationMemberEntity membership = organizationCapability.getDefaultOrganizationMembership(rootAccount.getId());

        return loginAsOrganizationMember(now, rootAccount.getId(), membership);
    }


    public ConnectionStatusDto loginToOrganizationWithPassword(String username, String password, UUID organizationId) {

        LocalDateTime now = gridClock.now();

        RootAccountEntity rootAccount = loginAndFindAccountWithUserPassword(username, password);

        //now I need to connect to the default organization for this account, and return the connect info

        OrganizationMemberEntity membership = organizationMemberRepository.findByOrganizationIdAndRootAccountId(organizationId, rootAccount.getId());

        return loginAsOrganizationMember(now, rootAccount.getId(), membership);
    }


    private RootAccountEntity loginAndFindAccountWithUserPassword(String username, String password) {
        log.debug("loginAndFindAccountWithUserPassword, user = {}", username);

        RootAccountEntity rootAccount = rootAccountRepository.findByLowercaseRootUsername(standarizeToLowerCase(username));

        if (rootAccount == null) {
            String maybeAnEmail = username;
            rootAccount = rootAccountRepository.findByRootEmail(standarizeToLowerCase(maybeAnEmail));
        }

        validateUserOrPasswordMatch(username, rootAccount);

        RootAccountEntity foundIfPasswordValid = rootAccountRepository.checkPasswordAndReturnIfValid(rootAccount.getId(), password);

        validateUserOrPasswordMatch(username, foundIfPasswordValid);
        return rootAccount;
    }


    public ConnectionStatusDto loginToOrganization(UUID rootAccountId, UUID organizationId) {

        LocalDateTime now = gridClock.now();

        OrganizationMemberEntity membership = organizationMemberRepository.findByOrganizationIdAndRootAccountId(organizationId, rootAccountId);

        return loginAsOrganizationMember(now, rootAccountId, membership);
    }

    private ConnectionStatusDto loginAsOrganizationMember(LocalDateTime now, UUID rootAccountId, OrganizationMemberEntity membership) {

        validateMembershipExists("login" , membership);

        ActiveAccountStatusEntity accountStatusEntity = findOrCreateActiveAccountStatus(now, rootAccountId);

        accountStatusEntity.setConnectionId(UUID.randomUUID());
        accountStatusEntity.setOnlineStatus(OnlineStatus.Connecting);
        accountStatusEntity.setLoggedInOrganizationId(membership.getOrganizationId());

        accountStatusRepository.save(accountStatusEntity);

        ConnectionStatusDto connectionStatus = createValidConnectionStatusDto(membership, accountStatusEntity);

        TeamDto team = teamMembership.getMyActiveTeam(membership.getOrganizationId(), membership.getId());
        if (team != null) {
            connectionStatus.setTeamId(team.getId());
        }

        List<OrganizationDto> participatingOrgs = organizationCapability.getParticipatingOrganizations(rootAccountId);
        connectionStatus.setParticipatingOrganizations(participatingOrgs);

        return connectionStatus;
    }

    private ConnectionStatusDto createValidConnectionStatusDto(OrganizationMemberEntity membership, ActiveAccountStatusEntity accountStatusEntity) {
        ConnectionStatusDto connectionStatus = new ConnectionStatusDto();
        connectionStatus.setMemberId(membership.getId());
        connectionStatus.setOrganizationId(membership.getOrganizationId());
        connectionStatus.setUsername(membership.getUsername());
        connectionStatus.setConnectionId(accountStatusEntity.getConnectionId());
        connectionStatus.setStatus(Status.VALID);
        connectionStatus.setMessage("Successfully logged in");
        return connectionStatus;
    }

    public ActiveTalkConnectionDto connect(UUID connectionId) {

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        ActiveAccountStatusEntity accountStatusEntity = accountStatusRepository.findByConnectionId(connectionId);

        validateConnectionExists(connectionId, accountStatusEntity);

        OrganizationMemberEntity membership = organizationCapability.getActiveMembership(accountStatusEntity.getRootAccountId());

        MemberConnectionEntity memberConnection = memberConnectionRepository.findByConnectionId(connectionId);

        accountStatusEntity.setOnlineStatus(OnlineStatus.Online);
        accountStatusRepository.save(accountStatusEntity);

        activeWorkStatusManager.pushTeamMemberStatusUpdate(membership.getOrganizationId(), membership.getId(), now, nanoTime);

        return getActiveTalkConnection(connectionId, memberConnection.getUsername());
    }

    private ActiveTalkConnectionDto getActiveTalkConnection(UUID newConnectionId, String username) {

        ActiveTalkConnectionDto activeTalkConnection = new ActiveTalkConnectionDto();
        activeTalkConnection.setUsername(username);
        activeTalkConnection.setConnectionId(newConnectionId);
        activeTalkConnection.setStatus(Status.VALID);
        activeTalkConnection.setMessage("Account connected.");

        return activeTalkConnection;
    }

    private void validateConnectionExists(UUID connectionId, ActiveAccountStatusEntity accountStatusEntity) {
        if (accountStatusEntity == null) {
            throw new BadRequestException(ValidationErrorCodes.INVALID_CONNECTION, "Unable to find connection " + connectionId);
        }
    }

    private void validateMembershipExists(String context, OrganizationMemberEntity membership) {
        if (membership == null) {
            throw new BadRequestException(ValidationErrorCodes.NO_ORG_MEMBERSHIP_FOR_ACCOUNT, "Unable to find org membership for "+context);
        }
    }

    @Transactional
    public SimpleStatusDto logout(UUID rootAccountId) {

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        ActiveAccountStatusEntity accountStatusEntity = findOrCreateActiveAccountStatus(now, rootAccountId);

        UUID oldConnectionId = accountStatusEntity.getConnectionId();


        accountStatusEntity.setOnlineStatus(OnlineStatus.Offline);
        accountStatusEntity.setConnectionId(null);

        accountStatusRepository.save(accountStatusEntity);

        MemberConnectionEntity memberConnection = memberConnectionRepository.findByConnectionId(oldConnectionId);

        if (memberConnection != null) {
            roomOperator.leaveAllRooms(now, nanoTime, memberConnection);
        }

        OrganizationMemberEntity membership = organizationCapability.getActiveMembership(rootAccountId);
        activeWorkStatusManager.pushTeamMemberStatusUpdate(membership.getOrganizationId(), membership.getId(), now, nanoTime);

        return new SimpleStatusDto(Status.SUCCESS, "Successfully logged out");
    }

//    @Scheduled(fixedDelay = 60000, initialDelay = 120000)
//    public void processHeartbeatConnectionDisconnects() {
//
//        log.info("Processing heartbeat Idles and Disconnects");
//
//        LocalDateTime now = gridClock.now();
//        Long nanoTime = gridClock.nanoTime();
//
//        LocalDateTime disconnectThreshold = now.minusMinutes(MINUTES_WITHOUT_HEARTBEAT_TO_DISCONNECT);
//
//        //handle online accounts gone idle
//
//        updateIdleStatusOnAccountsAndNotifyTeam(now, nanoTime);
//
//        //handle idle accounts disconnected (logout)
//
//        List<ActiveAccountStatusEntity> connectionsToLogout = accountStatusRepository.findLongMissingHeartbeat(Timestamp.valueOf(disconnectThreshold));
//
//        for (ActiveAccountStatusEntity account: connectionsToLogout) {
//
//            MemberConnectionEntity connection = updateOfflineStatusOnAccount(account);
//
//            notifyTeamOfOfflineStatus(now, nanoTime, connection);
//        }
//
//    }

    @Transactional
    void notifyTeamOfOfflineStatus(LocalDateTime now, Long nanoTime, MemberConnectionEntity connection) {
        activeWorkStatusManager.pushTeamMemberStatusUpdate(connection.getOrganizationId(), connection.getMemberId(), now, nanoTime);

        roomOperator.leaveAllRooms(now, nanoTime, connection);
    }

    @Transactional
    MemberConnectionEntity updateOfflineStatusOnAccount(ActiveAccountStatusEntity account) {
        MemberConnectionEntity connection = memberConnectionRepository.findByConnectionId(account.getConnectionId());

        account.setOnlineStatus(OnlineStatus.Offline);
        account.setConnectionId(null);

        accountStatusRepository.save(account);
        return connection;
    }


    @Transactional
    void updateIdleStatusOnAccountsAndNotifyTeam(LocalDateTime now, Long nanoTime) {

        LocalDateTime idleThreshold = now.minusMinutes(MINUTES_WITHOUT_HEARTBEAT_TO_IDLE);

        List<ActiveAccountStatusEntity> idleAccounts = accountStatusRepository.findMissingHeartbeat(Timestamp.valueOf(idleThreshold));

        for (ActiveAccountStatusEntity account: idleAccounts) {
            account.setOnlineStatus(OnlineStatus.Idle);
            accountStatusRepository.save(account);
        }

        for (ActiveAccountStatusEntity account: idleAccounts) {

            MemberConnectionEntity connection = memberConnectionRepository.findByConnectionId(account.getConnectionId());

            activeWorkStatusManager.pushTeamMemberStatusUpdate(connection.getOrganizationId(), connection.getMemberId(), now, nanoTime);

            roomOperator.updateStatusInAllRooms(now, nanoTime, connection);
        }
    }

    public SimpleStatusDto heartbeat(UUID rootAccountId, HeartbeatDto heartbeat) {
        SimpleStatusDto heartBeatStatus;

        LocalDateTime now = gridClock.now();

        ActiveAccountStatusEntity accountStatusEntity = findOrCreateActiveAccountStatus(now, rootAccountId);

        if (isNotOnline(accountStatusEntity)) {
            heartBeatStatus = new SimpleStatusDto(Status.FAILED, "Please re-login before updating heartbeat");
        } else {
            accountStatusEntity.setLastActivity(now);
            accountStatusEntity.setLastHeartbeat(now);
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

        OrganizationMemberEntity activeMembership = organizationCapability.getActiveMembership(rootAccountId);

        return toDto(rootAccountEntity, activeMembership);
    }

    private UserProfileDto toDto(RootAccountEntity rootAccount, OrganizationMemberEntity membership) {
        UserProfileDto userProfileDto = new UserProfileDto();
        userProfileDto.setRootAccountId(rootAccount.getId());
        userProfileDto.setFullName(rootAccount.getFullName());
        userProfileDto.setDisplayName(rootAccount.getDisplayName());
        userProfileDto.setRootEmail(rootAccount.getRootEmail());
        userProfileDto.setRootUsername(rootAccount.getRootUsername());

        if (membership != null) {
            userProfileDto.setOrgUsername(membership.getUsername());
            userProfileDto.setOrgEmail(membership.getEmail());
        }

        return userProfileDto;
    }

    public UserProfileDto updateRootProfileUsername(UUID rootAccountId, String username) {

        LocalDateTime now = gridClock.now();

        RootAccountEntity rootAccountEntity = rootAccountRepository.findById(rootAccountId);

        String oldUserName = rootAccountEntity.getRootUsername();

        rootAccountEntity.setRootUsername(username);
        rootAccountEntity.setLowercaseRootUsername(standarizeToLowerCase(username));
        rootAccountEntity.setLastUpdated(now);

        try {
            rootAccountRepository.save(rootAccountEntity);
        } catch (Exception ex) {
            String conflictMsg = "Conflict in renaming account username from " + oldUserName + " to " + username;
            log.warn(conflictMsg);

            throw new ConflictException(ConflictErrorCodes.CONFLICTING_USER_NAME, conflictMsg);
        }

        OrganizationEntity publicOrg = organizationCapability.findOrCreatePublicOrg(now);

        OrganizationMemberEntity pubOrgMembership = organizationMemberRepository.findByOrganizationIdAndRootAccountId(publicOrg.getId(), rootAccountEntity.getId());

        if (pubOrgMembership != null) {
            pubOrgMembership.setUsername(rootAccountEntity.getRootUsername());
            pubOrgMembership.setLowercaseUsername(rootAccountEntity.getLowercaseRootUsername());
            organizationMemberRepository.save(pubOrgMembership);
        }

        OrganizationMemberEntity activeMembership = organizationCapability.getActiveMembership(rootAccountId);

        return toDto(rootAccountEntity, activeMembership);
    }

    public UserProfileDto updateRootProfileEmail(UUID rootAccountId, String newEmail) {
        RootAccountEntity rootAccountEntity = rootAccountRepository.findById(rootAccountId);

        LocalDateTime now = gridClock.now();

        String standardizedEmail = standarizeToLowerCase(newEmail);

        OneTimeTicketEntity oneTimeTicket = oneTimeTicketCapability.issueOneTimeEmailValidationTicket(now, rootAccountId, standardizedEmail);

        emailCapability.sendEmailToValidateRootAccountProfileAddress(standardizedEmail, oneTimeTicket.getTicketCode());

        rootAccountRepository.save(rootAccountEntity);

        OrganizationMemberEntity activeMembership = organizationCapability.getActiveMembership(rootAccountId);

        UserProfileDto profile = toDto(rootAccountEntity, activeMembership);

        profile.setRootEmail(newEmail + " (pending validation)");

        return profile;
    }

    public SimpleStatusDto validateRootProfileEmail(String validationCode) {

        OneTimeTicketEntity oneTimeTicket = oneTimeTicketCapability.findByTicketCode(validationCode);

        SimpleStatusDto simpleStatus = new SimpleStatusDto();

        LocalDateTime now = gridClock.now();

        if (oneTimeTicket == null) {
            simpleStatus.setMessage("Validation code not found.");
            simpleStatus.setStatus(Status.FAILED);
        } else if (oneTimeTicketCapability.isExpired(now, oneTimeTicket)) {
            simpleStatus.setMessage("Validation code is expired.");
            simpleStatus.setStatus(Status.FAILED);

            oneTimeTicketCapability.expire(now, oneTimeTicket);

        } else {

            RootAccountEntity rootAccountEntity = rootAccountRepository.findById(oneTimeTicket.getOwnerId());

            validateAccountExists("validation ticket owner", rootAccountEntity);

            String email = oneTimeTicket.getEmailProp();

            rootAccountEntity.setRootEmail(email);
            rootAccountEntity.setLastUpdated(now);
            rootAccountEntity.setEmailValidated(true);

            rootAccountRepository.save(rootAccountEntity);

            oneTimeTicketCapability.use(now, oneTimeTicket, oneTimeTicket.getOwnerId());

            simpleStatus.setMessage("Profile Email successfully updated.");
            simpleStatus.setStatus(Status.SUCCESS);
        }

        return simpleStatus;
    }


    public UserProfileDto updateRootProfileFullName(UUID rootAccountId, String fullName) {

        RootAccountEntity rootAccountEntity = rootAccountRepository.findById(rootAccountId);

        validateAccountExists(rootAccountId.toString(), rootAccountEntity);
        validateNotNull("fullName", fullName);

        rootAccountEntity.setFullName(fullName);
        rootAccountEntity.setLastUpdated(gridClock.now());

        rootAccountRepository.save(rootAccountEntity);

        OrganizationMemberEntity activeMembership = organizationCapability.getActiveMembership(rootAccountId);

        return toDto(rootAccountEntity, activeMembership);
    }

    private void validateNotNull(String propertyName, String property) {
        if (property == null) {
            throw new BadRequestException(ValidationErrorCodes.PROPERTY_CANT_BE_NULL, "Property "+propertyName + " cant be null");
        }
    }

    public UserProfileDto updateRootProfileDisplayName(UUID rootAccountId, String displayName) {
        RootAccountEntity rootAccountEntity = rootAccountRepository.findById(rootAccountId);

        validateAccountExists(rootAccountId.toString(), rootAccountEntity);
        validateNotNull("displayName", displayName);

        rootAccountEntity.setDisplayName(displayName);
        rootAccountEntity.setLastUpdated(gridClock.now());

        rootAccountRepository.save(rootAccountEntity);

        OrganizationMemberEntity activeMembership = organizationCapability.getActiveMembership(rootAccountId);

        return toDto(rootAccountEntity, activeMembership);
    }

    private String standarizeToLowerCase(String name) {
        if (name != null) {
            return name.toLowerCase();
        }
        return null;
    }


    public UserProfileDto updateOrgProfileUsername(UUID rootAccountId, String username) {

        RootAccountEntity rootAccount = rootAccountRepository.findById(rootAccountId);

        OrganizationMemberEntity activeMembership = organizationCapability.getActiveMembership(rootAccountId);

        if (activeMembership != null) {

            String oldUserName = activeMembership.getUsername();

            activeMembership.setUsername(username);
            activeMembership.setLowercaseUsername(standarizeToLowerCase(username));

            try {
                organizationMemberRepository.save(activeMembership);
            } catch (Exception ex) {
                String conflictMsg = "Conflict in changing org member username from " + oldUserName + " to " + username;
                log.warn(conflictMsg);

                throw new ConflictException(ConflictErrorCodes.CONFLICTING_USER_NAME, conflictMsg);
            }

        }

        return toDto(rootAccount, activeMembership);
    }

    public UserProfileDto updateOrgProfileEmail(UUID rootAccountId, String newEmail) {

        RootAccountEntity rootAccount = rootAccountRepository.findById(rootAccountId);

        OrganizationMemberEntity activeMembership = organizationCapability.getActiveMembership(rootAccountId);

        UserProfileDto profile = toDto(rootAccount, activeMembership);

        if (activeMembership != null) {

            LocalDateTime now = gridClock.now();

            String standardizedEmail = standarizeToLowerCase(newEmail);

            OneTimeTicketEntity oneTimeTicket = oneTimeTicketCapability.issueOneTimeOrgEmailValidationTicket(now, rootAccountId, activeMembership.getOrganizationId(), standardizedEmail);

            emailCapability.sendEmailToValidateOrgAccountProfileAddress(standardizedEmail, oneTimeTicket.getTicketCode());

            profile.setOrgEmail(newEmail + " (pending validation)");
        }

        return profile;
    }

    public SimpleStatusDto validateOrgProfileEmail(String validationCode) {

        OneTimeTicketEntity oneTimeTicket = oneTimeTicketCapability.findByTicketCode(validationCode);

        SimpleStatusDto simpleStatus = new SimpleStatusDto();

        LocalDateTime now = gridClock.now();

        if (oneTimeTicket == null) {
            simpleStatus.setMessage("Validation code not found.");
            simpleStatus.setStatus(Status.FAILED);
        } else if (oneTimeTicketCapability.isExpired(now, oneTimeTicket)) {
            simpleStatus.setMessage("Validation code is expired.");
            simpleStatus.setStatus(Status.FAILED);

            oneTimeTicketCapability.expire(now, oneTimeTicket);

        } else {

            RootAccountEntity rootAccountEntity = rootAccountRepository.findById(oneTimeTicket.getOwnerId());

            validateAccountExists("org email validation ticket owner", rootAccountEntity);

            String email = oneTimeTicket.getEmailProp();
            UUID orgId = oneTimeTicket.getOrganizationIdProp();

            OrganizationMemberEntity membership = organizationMemberRepository.findByOrganizationIdAndRootAccountId(orgId, rootAccountEntity.getId());

            membership.setEmail(email);
            membership.setLastUpdated(now);

            organizationMemberRepository.save(membership);

            oneTimeTicketCapability.use(now, oneTimeTicket, oneTimeTicket.getOwnerId());

            simpleStatus.setMessage("Profile Email successfully updated.");
            simpleStatus.setStatus(Status.SUCCESS);
        }

        return simpleStatus;
    }


    private void validateTicketExistsAndNotExpired(LocalDateTime now, OneTimeTicketEntity existingInvitation) {
        if (existingInvitation == null || oneTimeTicketCapability.isExpired(now, existingInvitation)) {
            throw new BadRequestException(ValidationErrorCodes.INVALID_OR_EXPIRED_INVITATION_KEY, "Invitation is invalid or expired.");
        }
    }

    private void updatePassword(UUID rootAccountId, String password) {
        rootAccountRepository.updatePassword(rootAccountId, password);
    }

    private RootAccountEntity createAccountAndFlush(LocalDateTime now, String standardizedEmail, RootAccountCredentialsInputDto rootAccountInput) {
        RootAccountEntity existingRootAccount = rootAccountRepository.findByRootEmail(standardizedEmail);

        validateNoExistingAccount(standardizedEmail, existingRootAccount);

        RootAccountEntity newAccount = new RootAccountEntity();
        newAccount.setId(UUID.randomUUID());
        newAccount.setRootEmail(standardizedEmail);
        newAccount.setRootUsername(rootAccountInput.getUsername());
        newAccount.setLowercaseRootUsername(standarizeToLowerCase(rootAccountInput.getUsername()));
        newAccount.setFullName(rootAccountInput.getFullName());
        newAccount.setRegistrationDate(now);
        newAccount.setLastUpdated(now);
        newAccount.setEmailValidated(false);

        if (newAccount.getRootUsername() == null) {
            String generatedUsername = extractBaseUserFromEmail(standardizedEmail);

            newAccount.setRootUsername(generatedUsername);
            newAccount.setLowercaseRootUsername(generatedUsername);
        }

        newAccount = tryToSaveAndReserveUsername(newAccount);

        newAccount.setDisplayName(ObjectUtils.firstNonNull(rootAccountInput.getDisplayName(), rootAccountInput.getFullName(), newAccount.getRootUsername()));
        newAccount.setFullName(ObjectUtils.firstNonNull(rootAccountInput.getFullName(), newAccount.getRootUsername()));

        rootAccountRepository.save(newAccount);

        entityManager.flush();

        return newAccount;
    }

    private String extractBaseUserFromEmail(String standardizedEmail) {
        String user = "user";

        try {
            user = standardizedEmail.substring(0, standardizedEmail.indexOf('@'));
            ;
        } catch (Exception ex) {
            log.warn("Unable to parse name from email: " + standardizedEmail);
        }

        return user;
    }

    private RootAccountEntity tryToSaveAndReserveUsername(RootAccountEntity newAccount) {

        RootAccountEntity savedEntity = null;
        int retryCounter = 3;

        String requestedUsername = newAccount.getLowercaseRootUsername();

        while (savedEntity == null & retryCounter > 0) {

            RootAccountEntity existingUser = rootAccountRepository.findByLowercaseRootUsername(newAccount.getLowercaseRootUsername());

            if (existingUser == null) {
                savedEntity = rootAccountRepository.save(newAccount);
            } else {
                String randomExtension = createRandomExtension();
                newAccount.setRootUsername(requestedUsername + randomExtension);
                newAccount.setLowercaseRootUsername(requestedUsername + randomExtension);
                retryCounter--;
            }
        }

        if (savedEntity == null) {
            throw new ConflictException(ConflictErrorCodes.CONFLICTING_USER_NAME, "Unable to create account with requested username after 3 tries: " + requestedUsername);
        }

        return savedEntity;

    }

    private String createRandomExtension() {
        return Long.toString(Math.round(Math.random() * 89999 + 10000));
    }



}
