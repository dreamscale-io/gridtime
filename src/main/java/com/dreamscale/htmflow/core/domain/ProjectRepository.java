package com.dreamscale.htmflow.core.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends CrudRepository<ProjectEntity, UUID> {

    ProjectEntity findByExternalId(String externalId);

    List<ProjectEntity> findByOrganizationId(UUID organizationId);

    ProjectEntity findById(UUID id);
}
