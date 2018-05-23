package com.dreamscale.htmflow.core.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface RecentTaskRepository extends CrudRepository<RecentTaskEntity, UUID> {

    RecentTaskEntity findById(UUID id);

    List<RecentTaskEntity> findByMemberIdAndProjectId(UUID memberId, UUID projectId);
}
