package com.dreamscale.gridtime.core.domain.journal;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TeamProjectRepository extends CrudRepository<TeamProjectEntity, UUID> {

    TeamProjectEntity findByTeamIdAndLowercaseName(UUID teamId, String projectName);

    List<TeamProjectEntity> findByTeamId(UUID teamId);

    @Query(nativeQuery = true, value = "select p.* from team_project p, recent_project rp " +
            "where p.id = rp.project_id " +
            "and rp.member_id=(:memberId) " +
            "order by rp.last_accessed desc ")
    List<TeamProjectEntity> findByRecentMemberAccess(@Param("memberId") UUID memberId);

    @Query(nativeQuery = true, value = "select p.* from team_project p where exists " +
            "( select 1 from recent_project rp, team_member tm " +
            "where p.id = rp.project_id " +
            "and rp.member_id=tm.member_id " +
            "and tm.team_id = (:teamId) "+
            "and tm.organization_id = (:organizationId) "+
            "order by rp.last_accessed desc) limit 5" )
    List<TeamProjectEntity> findByRecentTeamMemberAccess(@Param("organizationId") UUID organizationId,
                                                     @Param("teamId") UUID teamId);
}
