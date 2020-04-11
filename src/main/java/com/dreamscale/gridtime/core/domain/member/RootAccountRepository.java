package com.dreamscale.gridtime.core.domain.member;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface RootAccountRepository extends CrudRepository<RootAccountEntity, UUID> {

    RootAccountEntity findById(UUID id);

    RootAccountEntity findByApiKey(String apiKey);

    RootAccountEntity findByRootEmail(String standarizedEmail);

}
