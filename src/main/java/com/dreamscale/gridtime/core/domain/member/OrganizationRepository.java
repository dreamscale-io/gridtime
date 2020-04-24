package com.dreamscale.gridtime.core.domain.member;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OrganizationRepository extends CrudRepository<OrganizationEntity, UUID> {

    @Query(nativeQuery = true, value = "select o.* from organization o where exists ( " +
            "select 1 from organization_member om where om.organization_id = o.id  "+
            "and om.root_account_id = (:rootAccountId) ) ")
    List<OrganizationEntity> findByParticipatingMembership(@Param("rootAccountId") UUID rootAccountId);

    OrganizationEntity findById(UUID id);

    OrganizationEntity findByDomainName(String domainName);
}
