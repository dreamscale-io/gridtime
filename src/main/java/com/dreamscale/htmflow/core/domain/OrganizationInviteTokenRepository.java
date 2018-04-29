package com.dreamscale.htmflow.core.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface OrganizationInviteTokenRepository extends CrudRepository<OrganizationInviteTokenEntity, UUID> {

    OrganizationInviteTokenEntity findById(String id);

    OrganizationInviteTokenEntity findByToken(String token);
}
