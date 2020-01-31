package com.dreamscale.gridtime.core.service;

import com.dreamscale.gridtime.api.account.AccountActivationDto;
import com.dreamscale.gridtime.api.account.ConnectionStatusDto;
import com.dreamscale.gridtime.api.account.HeartbeatDto;
import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.organization.OnlineStatus;
import com.dreamscale.gridtime.api.status.Status;
import com.dreamscale.gridtime.api.team.TeamDto;
import com.dreamscale.gridtime.core.domain.active.ActiveAccountStatusEntity;
import com.dreamscale.gridtime.core.domain.active.ActiveAccountStatusRepository;
import com.dreamscale.gridtime.core.domain.member.*;
import com.dreamscale.gridtime.core.security.MasterAccountIdResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class AccountService implements MasterAccountIdResolver {

    @Autowired
    private MasterAccountRepository masterAccountRepository;

    @Autowired
    private ActiveAccountStatusRepository accountStatusRepository;

    @Autowired
    private CircuitOperator circuitOperator;

    @Autowired
    private ActiveStatusService activeStatusService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private TeamService teamService;


    public AccountActivationDto activate(String activationCode) {
        MasterAccountEntity masterAccountEntity = masterAccountRepository.findByActivationCode(activationCode);

        AccountActivationDto accountActivationDto = new AccountActivationDto();

        if (masterAccountEntity == null) {
            accountActivationDto.setMessage("Activation code not found.");
            accountActivationDto.setStatus(Status.FAILED);
        } else {

            String apiKey = generateAPIKey();
            masterAccountEntity.setApiKey(apiKey);
            masterAccountEntity.setActivationDate(LocalDateTime.now());

            masterAccountRepository.save(masterAccountEntity);

            //TODO also clear activation code on successful activation so it's 1x use

            accountActivationDto.setEmail(masterAccountEntity.getMasterEmail());
            accountActivationDto.setApiKey(apiKey);
            accountActivationDto.setMessage("Your account has been successfully activated.");
            accountActivationDto.setStatus(Status.VALID);
        }

        return accountActivationDto;
    }

    public ConnectionStatusDto login(UUID masterAccountId) {

        UUID oldConnectionId = null;

        ActiveAccountStatusEntity accountStatusEntity = findOrCreateActiveAccountStatus(masterAccountId);

        accountStatusEntity.setConnectionId(UUID.randomUUID());
        accountStatusEntity.setOnlineStatus(OnlineStatus.Online);

        accountStatusRepository.save(accountStatusEntity);

        ConnectionStatusDto statusDto = new ConnectionStatusDto();
        statusDto.setConnectionId(accountStatusEntity.getConnectionId());
        statusDto.setStatus(Status.VALID);
        statusDto.setMessage("Successfully logged in");

        OrganizationMemberEntity membership = organizationService.getDefaultMembership(masterAccountId);
        statusDto.setMemberId(membership.getId());
        statusDto.setOrganizationId(membership.getOrganizationId());

        TeamDto team = teamService.getMyPrimaryTeam(membership.getOrganizationId(), membership.getId());

        if (team != null) {
            statusDto.setTeamId(team.getId());
        }

        activeStatusService.updateOnlineStatus(statusDto.getOrganizationId(), statusDto.getMemberId(), accountStatusEntity.getOnlineStatus());

        return statusDto;
    }

    public SimpleStatusDto logout(UUID masterAccountId) {

        ActiveAccountStatusEntity accountStatusEntity = findOrCreateActiveAccountStatus(masterAccountId);

        UUID oldConnectionId = accountStatusEntity.getConnectionId();

        if (oldConnectionId != null) {
            circuitOperator.notifyRoomsOfMemberDisconnect(oldConnectionId);
        }

        accountStatusEntity.setOnlineStatus(OnlineStatus.Offline);
        accountStatusEntity.setConnectionId(null);

        accountStatusRepository.save(accountStatusEntity);

        updateOnlineStatus(masterAccountId, OnlineStatus.Offline);

        return new SimpleStatusDto(Status.VALID, "Successfully logged out");
    }

    private void updateOnlineStatus(UUID masterAccountId, OnlineStatus onlineStatus) {
        OrganizationMemberEntity membership = organizationService.getDefaultMembership(masterAccountId);

        activeStatusService.updateOnlineStatus(membership.getOrganizationId(), membership.getId(), onlineStatus);

    }

    public SimpleStatusDto heartbeat(UUID masterAccountId, HeartbeatDto heartbeat) {
        SimpleStatusDto heartBeatStatus;

        ActiveAccountStatusEntity accountStatusEntity = findOrCreateActiveAccountStatus(masterAccountId);

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

    private ActiveAccountStatusEntity findOrCreateActiveAccountStatus(UUID masterAccountId) {

        ActiveAccountStatusEntity accountStatusEntity = accountStatusRepository.findByMasterAccountId(masterAccountId);
        if (accountStatusEntity == null) {
            accountStatusEntity = new ActiveAccountStatusEntity();
            accountStatusEntity.setMasterAccountId(masterAccountId);
            accountStatusEntity.setLastActivity(LocalDateTime.now());

        } else {
            accountStatusEntity.setLastActivity(LocalDateTime.now());
        }

        return accountStatusEntity;
    }

    public UUID findAccountIdByApiKey(String apiKey) {
        UUID masterAccountId = null;

        MasterAccountEntity masterAccountEntity = masterAccountRepository.findByApiKey(apiKey);
        if (masterAccountEntity != null) {
            masterAccountId = masterAccountEntity.getId();
        }
        return masterAccountId;
    }

    public LocalDateTime getActivationDate(UUID masterAccountId) {
        MasterAccountEntity masterAccountEntity = masterAccountRepository.findById(masterAccountId);
        return masterAccountEntity.getActivationDate();
    }

    @Override
    public UUID findAccountIdByConnectionId(String connectionId) {
        UUID masterAccountId = null;
        UUID connectUuid = UUID.fromString(connectionId);
        ActiveAccountStatusEntity accountStatusEntity = accountStatusRepository.findByConnectionId(connectUuid);
        if (accountStatusEntity != null) {
            masterAccountId = accountStatusEntity.getMasterAccountId();
        }
        return masterAccountId;
    }

    private String generateAPIKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }



}
