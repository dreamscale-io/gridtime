package com.dreamscale.gridtime.core.domain.journal;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends CrudRepository<TaskEntity, UUID> {

    List<TaskEntity> findByProjectId(UUID projectId);

    List<TaskEntity> findTop5ByProjectIdOrderByExternalIdDesc(UUID projectId);

    TaskEntity findByOrganizationIdAndProjectIdAndLowercaseName(UUID organizationId, UUID projectId, String taskName);

    List<TaskEntity> findByOrganizationIdAndName(UUID organizationId, String taskName);

    @Query(nativeQuery = true, value = "select t.* from task t, recent_task rt " +
            "where t.id = rt.task_id " +
            "and rt.member_id=(:memberId) " +
            "and rt.project_id=(:projectId) " +
            "order by rt.last_accessed desc ")
    List<TaskEntity> findByRecentMemberAccess(@Param("memberId") UUID memberId, @Param("projectId") UUID projectId);

    @Query(nativeQuery = true, value = "select t.* from task t where exists " +
            "(select 1 from recent_task rt, team_member tm " +
            "where t.id = rt.task_id " +
            "and rt.member_id = tm.member_id " +
            "and tm.team_id=(:teamId) " +
            "and rt.project_id=(:projectId) " +
            "order by rt.last_accessed desc ) limit 5")
    List<TaskEntity> findByRecentTeamAccess(@Param("teamId") UUID teamId, @Param("projectId") UUID projectId);


    List<TaskEntity> findTop10ByProjectIdAndLowercaseNameStartingWith(UUID projectId, String nameStartsWith);


    @Query(nativeQuery = true, value = "select t.* from task t, recent_task rt " +
            "where t.id = rt.task_id " +
            "and rt.member_id=(:memberId) " +
            "order by rt.last_accessed desc limit 1 ")
    TaskEntity findMostRecentTaskForMember(@Param("memberId") UUID memberId);
}
