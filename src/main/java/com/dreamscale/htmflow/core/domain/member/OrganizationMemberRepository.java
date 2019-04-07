package com.dreamscale.htmflow.core.domain.member;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface OrganizationMemberRepository extends CrudRepository<OrganizationMemberEntity, UUID> {

    OrganizationMemberEntity findById(UUID id);

    List<OrganizationMemberEntity> findByMasterAccountId(UUID masterAccountId);

    OrganizationMemberEntity findByOrganizationIdAndMasterAccountId(UUID organizationId, UUID masterAccountId);

}
