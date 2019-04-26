package com.dreamscale.htmflow.core.domain.tile;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface UriWithinProjectRepository extends CrudRepository<UriWithinProjectEntity, UUID> {

    UriWithinProjectEntity findByProjectIdAndObjectTypeAndObjectKey(UUID projectId, String objectType, String objectKey);

    UriWithinProjectEntity findByProjectIdAndUri(UUID projectId, String uri);
}
