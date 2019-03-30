package com.dreamscale.ideaflow.core.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface RecentProjectRepository extends CrudRepository<RecentProjectEntity, UUID> {

    RecentProjectEntity findById(UUID id);

    List<RecentProjectEntity> findByMemberId(UUID memberId);

    RecentProjectEntity findFirst1ByMemberIdOrderByLastAccessedDesc(UUID memberId);
}
