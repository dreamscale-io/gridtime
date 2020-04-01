package com.dreamscale.gridtime.core.domain.member;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface TeamMemberHomeRepository extends CrudRepository<TeamMemberHomeEntity, UUID> {

    TeamMemberHomeEntity findByOrganizationIdAndMemberId(UUID orgId, UUID memberId);
}
