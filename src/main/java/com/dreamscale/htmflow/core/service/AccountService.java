package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.account.AccountActivationDto;
import com.dreamscale.htmflow.api.account.ConnectionStatusDto;
import com.dreamscale.htmflow.api.account.HeartbeatDto;
import com.dreamscale.htmflow.api.account.SimpleStatusDto;
import com.dreamscale.htmflow.api.organization.OnlineStatus;
import com.dreamscale.htmflow.api.status.Status;
import com.dreamscale.htmflow.core.domain.*;
import com.dreamscale.htmflow.core.security.MasterAccountIdResolver;
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
        ActiveAccountStatusEntity accountStatusEntity = findOrCreateActiveAccountStatus(masterAccountId);

        accountStatusEntity.setConnectionId(UUID.randomUUID());
        accountStatusEntity.setOnlineStatus(OnlineStatus.Online);

        accountStatusRepository.save(accountStatusEntity);

        return new ConnectionStatusDto(Status.VALID, "Successfully logged in", accountStatusEntity.getConnectionId());
    }

    public SimpleStatusDto logout(UUID masterAccountId) {

        ActiveAccountStatusEntity accountStatusEntity = findOrCreateActiveAccountStatus(masterAccountId);

        accountStatusEntity.setOnlineStatus(OnlineStatus.Offline);
        accountStatusEntity.setConnectionId(null);

        accountStatusRepository.save(accountStatusEntity);

        return new SimpleStatusDto(Status.VALID, "Successfully logged out");
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
