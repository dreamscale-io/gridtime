package com.dreamscale.gridtime.core.domain.member;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface OrganizationInviteTokenRepository extends CrudRepository<OrganizationInviteTokenEntity, UUID> {

    OrganizationInviteTokenEntity findById(String id);

    OrganizationInviteTokenEntity findByToken(String token);

    OrganizationInviteTokenEntity findByOrganizationId(UUID organizationId);
}