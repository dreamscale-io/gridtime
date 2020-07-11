package com.dreamscale.gridtime.core.domain.active;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface RecentTaskRepository extends CrudRepository<RecentTaskEntity, UUID> {

    RecentTaskEntity findById(UUID id);

    List<RecentTaskEntity> findByMemberIdAndProjectId(UUID memberId, UUID projectId);


    RecentTaskEntity findFirst1ByOrganizationIdAndMemberIdOrderByLastAccessedDesc(UUID organizationId, UUID memberId);
}
