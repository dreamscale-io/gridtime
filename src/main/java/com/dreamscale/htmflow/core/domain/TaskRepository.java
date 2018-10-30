package com.dreamscale.htmflow.core.domain;

import com.dreamscale.htmflow.api.project.TaskDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends CrudRepository<TaskEntity, UUID> {

    List<TaskEntity> findByProjectId(UUID projectId);

    List<TaskEntity> findTop5ByProjectIdOrderByExternalIdDesc(UUID projectId);

    TaskEntity findByProjectIdAndName(UUID projectId, String taskName);

    List<TaskEntity> findByOrganizationIdAndName(UUID organizationId, String taskName);

    @Query(nativeQuery = true, value = "select t.* from task t, recent_task rt " +
            "where t.id = rt.task_id " +
            "and rt.member_id=(:memberId) " +
            "and rt.project_id=(:projectId) " +
            "order by rt.last_accessed desc ")
    List<TaskEntity> findByRecentMemberAccess(@Param("memberId") UUID memberId, @Param("projectId") UUID projectId);

    List<TaskEntity> findTop10ByProjectIdAndNameStartingWith(UUID projectId, String nameStartsWith);
}
