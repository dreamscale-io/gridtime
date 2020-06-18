package com.dreamscale.gridtime.core.domain.journal;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface ProjectGrantAccessRepository extends CrudRepository<ProjectGrantAccessEntity, UUID> {


    ProjectGrantAccessEntity findByProjectIdAndGrantTypeAndGrantedToId(UUID projectId, GrantType grantType, UUID grantTo);
}
