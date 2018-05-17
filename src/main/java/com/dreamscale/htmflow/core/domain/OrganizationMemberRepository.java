package com.dreamscale.htmflow.core.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OrganizationMemberRepository extends CrudRepository<OrganizationMemberEntity, UUID> {

    OrganizationMemberEntity findById(String id);

    List<OrganizationMemberEntity> findByMasterAccountId(UUID masterAccountId);

    OrganizationMemberEntity findByOrganizationIdAndMasterAccountId(UUID organizationId, UUID masterAccountId);

}
