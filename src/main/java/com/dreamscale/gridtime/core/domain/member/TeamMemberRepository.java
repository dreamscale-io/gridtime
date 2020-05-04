package com.dreamscale.gridtime.core.domain.member;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface TeamMemberRepository extends CrudRepository<TeamMemberEntity, UUID> {

    TeamMemberEntity findById(UUID id);

    List<TeamMemberEntity> findByOrganizationIdAndMemberId(UUID organizationId, UUID memberId);

    List<TeamMemberEntity> findByMemberId(UUID memberId);

    List<TeamMemberEntity> findByTeamId(UUID teamId);

    TeamMemberEntity findByTeamIdAndMemberId(UUID teamId, UUID memberId);

}
