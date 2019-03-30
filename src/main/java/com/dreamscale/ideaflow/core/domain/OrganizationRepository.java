package com.dreamscale.ideaflow.core.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface OrganizationRepository extends CrudRepository<OrganizationEntity, UUID> {

    OrganizationEntity findById(UUID id);

    OrganizationEntity findByDomainName(String domainName);
}
