package com.dreamscale.htmflow.core.domain.journal;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface ConfigProjectSyncRepository extends CrudRepository<ConfigProjectSyncEntity, UUID> {

    ConfigProjectSyncEntity findByOrganizationIdAndProjectExternalId(UUID organizationId, String projectExternalId);

    List<ConfigProjectSyncEntity> findByOrganizationId(UUID organizationId);
}
