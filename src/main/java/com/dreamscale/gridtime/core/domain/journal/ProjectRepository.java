package com.dreamscale.gridtime.core.domain.journal;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends CrudRepository<ProjectEntity, UUID> {

    ProjectEntity findByExternalId(String externalId);


    @Query(nativeQuery = true, value = "select p.* from project p " +
            "where p.organization_id = (:organizationId) " +
            "and (p.is_private = false or (exists (select 1 from project_grant_access g " +
            "where g.project_id = p.id and g.grant_type = 'MEMBER' and g.granted_to_id=(:memberId))) " +
            "or exists (select 1 from project_grant_access gt, team_member tm " +
            "where gt.project_id = p.id " +
            "and gt.grant_type = 'TEAM'" +
            "and gt.granted_to_id = tm.team_id " +
            "and tm.member_id = (:memberId))) "+
            "order by p.created_date limit (:limit)")
    List<ProjectEntity> findByOrganizationIdAndPermissionWithLimit(@Param("organizationId") UUID organizationId, @Param("memberId") UUID memberId, @Param("limit") int limit);


    @Query(nativeQuery = true, value = "select p.* from project p " +
            "where p.organization_id = (:organizationId) " +
            "and (p.is_private = false or (exists (select 1 from project_grant_access g " +
            "where g.project_id = p.id and g.grant_type = 'MEMBER' and g.granted_to_id=(:memberId))) " +
            "or exists (select 1 from project_grant_access gt, team_member tm " +
            "where gt.project_id = p.id " +
            "and gt.grant_type = 'TEAM'" +
            "and gt.granted_to_id = tm.team_id " +
            "and tm.member_id = (:memberId))) "+
            "order by p.name")
    List<ProjectEntity> findByOrganizationIdAndPermission(@Param("organizationId") UUID organizationId, @Param("memberId") UUID memberId);


    @Query(nativeQuery = true, value = "select p.* from project p " +
            "where p.organization_id = (:organizationId) " +
            "and p.is_private = false " +
            "order by p.name")
    List<ProjectEntity> findPublicProjectsByOrganizationId(@Param("organizationId") UUID organizationId);


    @Query(nativeQuery = true, value = "select p.* from project p " +
            "where p.organization_id = (:organizationId) " +
            "and p.is_private = false "+
            "and p.lowercase_name=(:lowercaseProjectName) ")
    ProjectEntity findPublicProjectByName(@Param("organizationId") UUID organizationId, @Param("lowercaseProjectName") String lowercaseProjectName);

    @Query(nativeQuery = true, value = "select p.* from project p " +
            "where p.organization_id = (:organizationId) " +
            "and p.is_private = true "+
            "and p.lowercase_name=(:lowercaseProjectName) " +
            "and (exists (select 1 from project_grant_access g " +
            "where g.project_id = p.id " +
            "and g.grant_type = 'MEMBER' " +
            "and g.granted_to_id=(:memberId)) " +
            "or exists (select 1 from project_grant_access gt, team_member tm " +
            "where gt.project_id = p.id " +
            "and gt.grant_type = 'TEAM'" +
            "and gt.granted_to_id = tm.team_id " +
            "and tm.member_id = (:memberId))) "+
            "order by p.created_date")
    List<ProjectEntity> findPrivateProjectByName(@Param("organizationId") UUID organizationId, @Param("memberId") UUID memberId, @Param("lowercaseProjectName") String lowercaseProjectName);

    @Query(nativeQuery = true, value = "select p.* from project p " +
            "where p.organization_id = (:organizationId) " +
            "and p.is_private = true "+
            "and p.id = (:projectId) "+
            "and (exists (select 1 from project_grant_access g " +
            "where g.project_id = p.id " +
            "and g.grant_type = 'MEMBER' " +
            "and g.granted_to_id=(:memberId)) " +
            "or exists (select 1 from project_grant_access gt, team_member tm " +
            "where gt.project_id = p.id " +
            "and gt.grant_type = 'TEAM'" +
            "and gt.granted_to_id = tm.team_id " +
            "and tm.member_id = (:memberId)))")
    ProjectEntity findPrivateProjectById(@Param("organizationId") UUID organizationId, @Param("memberId") UUID memberId, @Param("projectId") UUID projectId);

    ProjectEntity findByOrganizationIdAndId(UUID organizationId, UUID id);

    @Query(nativeQuery = true, value = "select p.* from project p, recent_project rp " +
            "where p.id = rp.project_id " +
            "and rp.member_id=(:memberId) " +
            "and rp.organization_id=(:organizationId) "+
            "order by rp.last_accessed desc ")
    List<ProjectEntity> findByRecentMemberAccess(@Param("organizationId") UUID organizationId, @Param("memberId") UUID memberId);


}
