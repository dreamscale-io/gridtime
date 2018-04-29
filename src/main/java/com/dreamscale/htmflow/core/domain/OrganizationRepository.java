package com.dreamscale.htmflow.core.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface OrganizationRepository extends CrudRepository<OrganizationEntity, UUID> {

    OrganizationEntity findById(UUID id);
}
