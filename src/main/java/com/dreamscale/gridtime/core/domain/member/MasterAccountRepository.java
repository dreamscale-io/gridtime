package com.dreamscale.gridtime.core.domain.member;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface MasterAccountRepository extends CrudRepository<MasterAccountEntity, UUID> {

    MasterAccountEntity findById(UUID id);

    MasterAccountEntity findByApiKey(String apiKey);

    MasterAccountEntity findByActivationCode(String activationCode);
}
