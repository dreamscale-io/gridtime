package com.dreamscale.gridtime.core.domain.member;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MemberStatusRepository extends CrudRepository<MemberStatusEntity, UUID> {

    List<MemberStatusEntity> findByOrganizationId(UUID organizationId);

    @Query(nativeQuery = true, value = "select msv.* from member_status_view msv, " +
            "team_member tm "+
            "where tm.member_id = msv.id "+
            "and tm.team_id=(:teamId) ")
    List<MemberStatusEntity> findByTeamId(@Param("teamId") UUID teamId);

    @Query(nativeQuery = true, value = "select msv.* from member_status_view msv, " +
            "team_member tm "+
            "where tm.member_id = msv.id "+
            "and tm.team_id=(:teamId) " +
            "and tm.member_id != (:me) ")
    List<MemberStatusEntity> findByTeamIdAndNotMe(@Param("teamId") UUID teamId, @Param("me") UUID myId );

    MemberStatusEntity findById(UUID memberId);

    MemberStatusEntity findByOrganizationIdAndId(UUID organizationId, UUID memberId);

}
