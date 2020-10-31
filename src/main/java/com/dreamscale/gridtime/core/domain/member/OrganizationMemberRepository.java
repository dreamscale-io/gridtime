package com.dreamscale.gridtime.core.domain.member;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OrganizationMemberRepository extends CrudRepository<OrganizationMemberEntity, UUID> {

    @Query(nativeQuery = true, value = "select * from organization_member om " +
            "where not exists (select 1 from torchie_feed_cursor tfc where om.id = tfc.torchie_id) ")
    List<OrganizationMemberEntity> selectMissingTorchies();

    List<OrganizationMemberEntity> findByOrganizationId(UUID organizationId);

    OrganizationMemberEntity findByOrganizationIdAndId(UUID organizationId, UUID id);

    OrganizationMemberEntity findById(UUID id);

    List<OrganizationMemberEntity> findByRootAccountId(UUID rootAccountId);

    OrganizationMemberEntity findByOrganizationIdAndRootAccountId(UUID organizationId, UUID rootAccountId);

    OrganizationMemberEntity findByOrganizationIdAndLowercaseUsername(UUID organizationId, String username);

    @Query(nativeQuery = true, value = "select om.* from organization_member om where " +
            "om.root_account_id = (:rootAccountId) and exists ( " +
            "select 1 from active_account_status aas where aas.root_account_id = om.root_account_id  "+
            "and aas.logged_in_organization_id = om.organization_id ) ")
    OrganizationMemberEntity findByActiveOrganizationAndRootAccountId(@Param("rootAccountId") UUID rootAccountId);

    OrganizationMemberEntity findByOrganizationIdAndEmail(UUID organizationId, String email);

}
