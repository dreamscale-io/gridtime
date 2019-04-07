package com.dreamscale.htmflow.core.domain.uri;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface UriWithinProjectRepository extends CrudRepository<UriWithinProjectEntity, UUID> {

    UriWithinProjectEntity findByProjectIdAndObjectTypeAndObjectKey(UUID projectId, UriObjectType objectType, String objectKey);
}
