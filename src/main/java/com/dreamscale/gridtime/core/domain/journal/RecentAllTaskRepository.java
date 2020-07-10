package com.dreamscale.gridtime.core.domain.journal;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RecentAllTaskRepository extends CrudRepository<RecentAllTaskEntity, UUID> {

    @Query(nativeQuery = true, value = "select rt.* from recent_all_task_view rt " +
            "where rt.member_id=(:memberId) " +
            "and rt.project_id=(:projectId) " +
            "and rt.organization_id=(:organizationId) "+
            "order by rt.last_accessed desc ")
    List<RecentAllTaskEntity> findByRecentMemberAccess(@Param("organizationId") UUID organizationId, @Param("memberId") UUID memberId, @Param("projectId") UUID projectId);

    @Query(nativeQuery = true, value = "select rt.* from recent_all_task_view rt " +
            "where rt.member_id=(:memberId) " +
            "and rt.organization_id=(:organizationId) "+
            "order by rt.last_accessed desc limit 1")
    RecentAllTaskEntity findMostRecentTaskForMember(@Param("organizationId") UUID organizationId, @Param("memberId") UUID memberId);
}
