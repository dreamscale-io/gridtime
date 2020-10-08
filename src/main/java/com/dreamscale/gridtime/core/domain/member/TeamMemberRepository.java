package com.dreamscale.gridtime.core.domain.member;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface TeamMemberRepository extends CrudRepository<TeamMemberEntity, UUID> {

    @Query(nativeQuery = true, value = "select * from team_member tm " +
            "where not exists (select 1 from torchie_feed_cursor tfc where tm.member_id = tfc.torchie_id) ")
    List<TeamMemberEntity> selectMissingTorchies();

    TeamMemberEntity findById(UUID id);

    List<TeamMemberEntity> findByOrganizationIdAndMemberId(UUID organizationId, UUID memberId);

    List<TeamMemberEntity> findByMemberId(UUID memberId);

    List<TeamMemberEntity> findByTeamId(UUID teamId);

    TeamMemberEntity findByTeamIdAndMemberId(UUID teamId, UUID memberId);

}
