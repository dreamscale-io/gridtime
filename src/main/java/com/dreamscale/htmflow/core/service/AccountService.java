package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.account.AccountActivationDto;
import com.dreamscale.htmflow.api.status.Status;
import com.dreamscale.htmflow.core.domain.MasterAccountEntity;
import com.dreamscale.htmflow.core.domain.MasterAccountRepository;
import com.dreamscale.htmflow.core.security.UserIdResolver;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.ErrorEntity;
import org.dreamscale.exception.WebApplicationException;
import org.dreamscale.logging.LoggingLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class AccountService implements UserIdResolver {

    @Autowired
    private MasterAccountRepository masterAccountRepository;

    public AccountActivationDto activate(String activationCode) {
        MasterAccountEntity masterAccountEntity = masterAccountRepository.findByActivationCode(activationCode);
        if (masterAccountEntity == null) {
            throw new WebApplicationException(404, new ErrorEntity("404", null, "activationCode not found", null, LoggingLevel.WARN));
        }

        String apiKey = generateAPIKey();
        masterAccountEntity.setApiKey(apiKey);
        masterAccountEntity.setActivationDate(LocalDateTime.now());

        masterAccountRepository.save(masterAccountEntity);

        //TODO also clear activation code on successful activation so it's 1x use

        AccountActivationDto accountActivationDto = new AccountActivationDto();
        accountActivationDto.setEmail(masterAccountEntity.getMasterEmail());
        accountActivationDto.setApiKey(apiKey);

        return accountActivationDto;
    }

    public UUID findAccountIdByApiKey(String apiKey) {
        UUID accountId = null;

        MasterAccountEntity masterAccountEntity = masterAccountRepository.findByApiKey(apiKey);
        if (masterAccountEntity != null) {
            accountId = masterAccountEntity.getId();
        }
        return accountId;
    }

    private String generateAPIKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
