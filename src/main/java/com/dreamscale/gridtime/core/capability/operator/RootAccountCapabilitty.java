package com.dreamscale.gridtime.core.capability.operator;

import com.dreamscale.gridtime.api.account.AccountActivationDto;
import com.dreamscale.gridtime.api.account.ConnectionStatusDto;
import com.dreamscale.gridtime.api.account.HeartbeatDto;
import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.organization.OnlineStatus;
import com.dreamscale.gridtime.api.status.Status;
import com.dreamscale.gridtime.api.team.TeamDto;
import com.dreamscale.gridtime.core.capability.directory.OrganizationDirectoryCapability;
import com.dreamscale.gridtime.core.capability.directory.TeamDirectoryCapability;
import com.dreamscale.gridtime.core.domain.active.ActiveAccountStatusEntity;
import com.dreamscale.gridtime.core.domain.active.ActiveAccountStatusRepository;
import com.dreamscale.gridtime.core.domain.member.*;
import com.dreamscale.gridtime.core.security.RootAccountIdResolver;
import com.dreamscale.gridtime.core.capability.active.ActiveWorkStatusManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class RootAccountCapabilitty implements RootAccountIdResolver {

    @Autowired
    private RootAccountRepository rootAccountRepository;

    @Autowired
    private ActiveAccountStatusRepository accountStatusRepository;

    @Autowired
    private LearningCircuitOperator learningCircuitOperator;

    @Autowired
    private ActiveWorkStatusManager activeWorkStatusManager;

    @Autowired
    private OrganizationDirectoryCapability organizationDirectoryCapability;

    @Autowired
    private TeamDirectoryCapability teamDirectoryCapability;


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

        UUID oldConnectionId = null;

        ActiveAccountStatusEntity accountStatusEntity = findOrCreateActiveAccountStatus(rootAccountId);

        accountStatusEntity.setConnectionId(UUID.randomUUID());
        accountStatusEntity.setOnlineStatus(OnlineStatus.Online);

        accountStatusRepository.save(accountStatusEntity);

        ConnectionStatusDto statusDto = new ConnectionStatusDto();
        statusDto.setConnectionId(accountStatusEntity.getConnectionId());
        statusDto.setStatus(Status.VALID);
        statusDto.setMessage("Successfully logged in");

        OrganizationMemberEntity membership = organizationDirectoryCapability.getDefaultMembership(rootAccountId);
        statusDto.setMemberId(membership.getId());
        statusDto.setOrganizationId(membership.getOrganizationId());
        statusDto.setUserName(membership.getUsername());

        TeamDto team = teamDirectoryCapability.getMyPrimaryTeam(membership.getOrganizationId(), membership.getId());

        if (team != null) {
            statusDto.setTeamId(team.getId());
        }

        activeWorkStatusManager.updateOnlineStatus(statusDto.getOrganizationId(), statusDto.getMemberId(), accountStatusEntity.getOnlineStatus());

        return statusDto;
    }

    public SimpleStatusDto logout(UUID rootAccountId) {

        ActiveAccountStatusEntity accountStatusEntity = findOrCreateActiveAccountStatus(rootAccountId);

        UUID oldConnectionId = accountStatusEntity.getConnectionId();

        if (oldConnectionId != null) {
            learningCircuitOperator.notifyRoomsOfMemberDisconnect(oldConnectionId);
        }

        accountStatusEntity.setOnlineStatus(OnlineStatus.Offline);
        accountStatusEntity.setConnectionId(null);

        accountStatusRepository.save(accountStatusEntity);

        updateOnlineStatus(rootAccountId, OnlineStatus.Offline);

        return new SimpleStatusDto(Status.VALID, "Successfully logged out");
    }

    private void updateOnlineStatus(UUID rootAccountId, OnlineStatus onlineStatus) {
        OrganizationMemberEntity membership = organizationDirectoryCapability.getDefaultMembership(rootAccountId);

        activeWorkStatusManager.updateOnlineStatus(membership.getOrganizationId(), membership.getId(), onlineStatus);

    }

    public SimpleStatusDto heartbeat(UUID rootAccountId, HeartbeatDto heartbeat) {
        SimpleStatusDto heartBeatStatus;

        ActiveAccountStatusEntity accountStatusEntity = findOrCreateActiveAccountStatus(rootAccountId);

        if (isNotOnline(accountStatusEntity)) {
            heartBeatStatus = new SimpleStatusDto(Status.FAILED, "Please login before updating heartbeat");
        } else {
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

    private ActiveAccountStatusEntity findOrCreateActiveAccountStatus(UUID rootAccountId) {

        ActiveAccountStatusEntity accountStatusEntity = accountStatusRepository.findByRootAccountId(rootAccountId);
        if (accountStatusEntity == null) {
            accountStatusEntity = new ActiveAccountStatusEntity();
            accountStatusEntity.setRootAccountId(rootAccountId);
            accountStatusEntity.setLastActivity(LocalDateTime.now());

        } else {
            accountStatusEntity.setLastActivity(LocalDateTime.now());
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



}
