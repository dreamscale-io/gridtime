package com.dreamscale.gridtime.core.domain.journal;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends CrudRepository<ProjectEntity, UUID> {

    ProjectEntity findByExternalId(String externalId);

    List<ProjectEntity> findByOrganizationIdOrderByName(UUID organizationId);

    ProjectEntity findByOrganizationIdAndLowercaseName(UUID orgId, String name);

    ProjectEntity findById(UUID id);

    @Query(nativeQuery = true, value = "select p.* from project p, recent_project rp " +
            "where p.id = rp.project_id " +
            "and rp.member_id=(:memberId) " +
            "order by rp.last_accessed desc ")
    List<ProjectEntity> findByRecentMemberAccess(@Param("memberId") UUID memberId);


}
