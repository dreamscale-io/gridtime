package com.dreamscale.gridtime.core.domain.member;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface OrganizationMemberRepository extends CrudRepository<OrganizationMemberEntity, UUID> {

    OrganizationMemberEntity findByOrganizationIdAndId(UUID organizationId, UUID id);

    OrganizationMemberEntity findById(UUID id);

    List<OrganizationMemberEntity> findByRootAccountId(UUID rootAccountId);

    OrganizationMemberEntity findByOrganizationIdAndRootAccountId(UUID organizationId, UUID rootAccountId);

    OrganizationMemberEntity findByOrganizationIdAndUsername(UUID organizationId, String userName);
}
