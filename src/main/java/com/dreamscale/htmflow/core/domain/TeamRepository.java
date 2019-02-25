package com.dreamscale.htmflow.core.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TeamRepository extends CrudRepository<TeamEntity, UUID> {

    TeamEntity findById(UUID id);

    List<TeamEntity> findByOrganizationId(UUID orgId);

        @Query(nativeQuery = true, value = "select t.* from team t, team_member tm, organization_member om " +
            "where t.id = tm.team_id " +
            "and tm.member_id = om.id "+
            "and om.organization_id = (:orgId) "+
            "and om.master_account_id =(:masterAccountId) ")
    List<TeamEntity> findMyTeamsByMembership(@Param("orgId") UUID orgId, @Param("masterAccountId") UUID masterAccountId);


    @Query(nativeQuery = true, value = "select t.* from team t, team_member tm, organization_member om " +
            "where t.id = tm.team_id " +
            "and tm.member_id = om.id "+
            "and om.organization_id = (:orgId) "+
            "and om.id =(:memberId) ")
    List<TeamEntity> findMyTeamsByOrgMembership(@Param("orgId") UUID orgId, @Param("memberId") UUID memberId);

}
