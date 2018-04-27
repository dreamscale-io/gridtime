package com.dreamscale.htmflow.core.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface MasterAccountRepository extends CrudRepository<MasterAccountEntity, UUID> {

    MasterAccountEntity findById(String id);
}
