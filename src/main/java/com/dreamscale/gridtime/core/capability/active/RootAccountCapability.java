package com.dreamscale.gridtime.core.capability.active;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.*;
import com.dreamscale.gridtime.api.organization.OnlineStatus;
import com.dreamscale.gridtime.api.status.Status;
import com.dreamscale.gridtime.api.team.TeamDto;
import com.dreamscale.gridtime.core.capability.directory.OrganizationMembershipCapability;
import com.dreamscale.gridtime.core.capability.directory.TeamMembershipCapability;
import com.dreamscale.gridtime.core.capability.integration.EmailCapability;
import com.dreamscale.gridtime.core.capability.operator.LearningCircuitOperator;
import com.dreamscale.gridtime.core.domain.active.ActiveAccountStatusEntity;
import com.dreamscale.gridtime.core.domain.active.ActiveAccountStatusRepository;
import com.dreamscale.gridtime.core.domain.circuit.MemberConnectionRepository;
import com.dreamscale.gridtime.core.domain.member.*;
import com.dreamscale.gridtime.core.exception.ConflictErrorCodeGroups;
import com.dreamscale.gridtime.core.exception.ConflictErrorCodes;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.security.RootAccountIdResolver;
import com.dreamscale.gridtime.core.service.GridClock;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.dreamscale.exception.ConflictException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    private GridClock gridClock;

    @Autowired
    private EmailCapability emailCapability;


    public SimpleStatusDto registerAccount(String rootEmail) {

        //so if I register an account, with just an email, and that's it

        //then I get an email with an activation token, to paste into my tool.

        //the activation token must come from the email, and will link to that email.

        //so I need to create a root account, in the invalidated state.

        //we should only let the activation tokens work 1x.

        //unless the account is reset, in which case, you can send a new activation token

        //emailCapability.sendDownloadAndActivationEmail(rootEmail);

        return null;
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }



    private String constructInvitationLink(String inviteToken) {
        String baseInviteLink = ResourcePaths.ORGANIZATION_PATH + ResourcePaths.MEMBER_PATH + ResourcePaths.INVITATION_PATH;
        return baseInviteLink + "?token=" + inviteToken;
    }

    public AccountActivationDto activate(String activationCode) {
        RootAccountEntity rootAccountEntity = rootAccountRepository.findByActivationCode(activationCode);

        AccountActivationDto accountActivationDto = new AccountActivationDto();

        if (rootAccountEntity == null) {
            accountActivationDto.setMessage("Activation code not found.");
            accountActivationDto.setStatus(Status.FAILED);
        } else {

            String apiKey = generateAPIKey();
            rootAccountEntity.setApiKey(apiKey);
            rootAccountEntity.setActivationDate(LocalDateTime.now());

            rootAccountRepository.save(rootAccountEntity);

            //TODO also clear activation code on successful activation so it's 1x use

            accountActivationDto.setEmail(rootAccountEntity.getRootEmail());
            accountActivationDto.setApiKey(apiKey);
            accountActivationDto.setMessage("Your account has been successfully activated.");
            accountActivationDto.setStatus(Status.VALID);
        }

        return accountActivationDto;
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

    public RoomConnectionScopeDto connect(UUID connectionId) {

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        ActiveAccountStatusEntity accountStatusEntity = accountStatusRepository.findByConnectionId(connectionId);

        validateConnectionExists(connectionId, accountStatusEntity);

        accountStatusEntity.setOnlineStatus(OnlineStatus.Online);
        accountStatusRepository.save(accountStatusEntity);

        OrganizationMemberEntity membership = organizationMembership.getDefaultMembership(accountStatusEntity.getRootAccountId());

        activeWorkStatusManager.pushTeamMemberStatusUpdate(membership.getOrganizationId(), membership.getId(), now, nanoTime);

        return learningCircuitOperator.notifyRoomsOfMemberReconnect(connectionId);

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

        return new SimpleStatusDto(Status.VALID, "Successfully logged out");
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

            heartBeatStatus = new SimpleStatusDto(Status.VALID, "beat.");
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
            String conflictMsg = "Unable to change root account username from "+oldUserName+" to "+ username;
            log.warn(conflictMsg);

            throw new ConflictException(ConflictErrorCodes.CONFLICTING_USER_NAME, conflictMsg);
        }

        return toDto(rootAccountEntity);
    }

    public UserProfileDto updateProfileEmail(UUID rootAccountId, String email) {

        RootAccountEntity rootAccountEntity = rootAccountRepository.findById(rootAccountId);

        rootAccountEntity.setRootEmail(standarizeToLowerCase(email));
        rootAccountEntity.setLastUpdated(gridClock.now());

        rootAccountRepository.save(rootAccountEntity);

        return toDto(rootAccountEntity);
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
