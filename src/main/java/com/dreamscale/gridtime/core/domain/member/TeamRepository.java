package com.dreamscale.gridtime.core.domain.member;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TeamRepository extends CrudRepository<TeamEntity, UUID> {

    TeamEntity findById(UUID id);

    List<TeamEntity> findByOrganizationId(UUID orgId);

    TeamEntity findByOrganizationIdAndName(UUID organizationId, String name);

        @Query(nativeQuery = true, value = "select t.* from team t, team_member tm, organization_member om " +
            "where t.id = tm.team_id " +
            "and tm.member_id = om.id "+
            "and om.organization_id = (:orgId) "+
            "and om.root_account_id =(:rootAccountId) ")
    List<TeamEntity> findMyTeamsByMembership(@Param("orgId") UUID orgId, @Param("rootAccountId") UUID rootAccountId);


    @Query(nativeQuery = true, value = "select t.* from team t, team_member tm, organization_member om " +
            "where t.id = tm.team_id " +
            "and tm.member_id = om.id "+
            "and om.organization_id = (:orgId) "+
            "and om.id =(:memberId) order by t.name")
    List<TeamEntity> findMyTeamsByOrgMembership(@Param("orgId") UUID orgId, @Param("memberId") UUID memberId);


    @Query(nativeQuery = true, value = "select t.* from team t where exists ( " +
            "select 1 from team_member tm1, team_member tm2 " +
            "where tm1.team_id = tm2.team_id " +
            "and tm1.member_id = (:member1) "+
            "and tm2.member_id = (:member2) "+
            "and tm1.organization_id = (:orgId) "+
            "and tm2.organization_id = (:orgId) "+
            "and tm1.team_id = t.id "+
            "and tm2.team_id = t.id )")
    List<TeamEntity> findTeamsContainingBothMembers(@Param("orgId") UUID orgId, @Param("member1") UUID member1,
                                                    @Param("member2") UUID member2 );


}
