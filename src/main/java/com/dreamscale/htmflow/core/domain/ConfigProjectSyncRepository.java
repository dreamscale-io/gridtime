package com.dreamscale.htmflow.core.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface ConfigProjectSyncRepository extends CrudRepository<ConfigProjectSyncEntity, UUID> {

    ConfigProjectSyncEntity findByOrganizationIdAndProjectName(UUID organizationId, String projectName);

    List<ConfigProjectSyncEntity> findByOrganizationId(UUID organizationId);
}
