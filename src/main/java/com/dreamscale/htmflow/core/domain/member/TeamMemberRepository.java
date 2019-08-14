package com.dreamscale.htmflow.core.domain.member;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface TeamMemberRepository extends CrudRepository<TeamMemberEntity, UUID> {

    TeamMemberEntity findById(UUID id);

    List<TeamMemberEntity> findByMemberId(UUID memberId);
}
