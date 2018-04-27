package com.dreamscale.htmflow.core.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface OrganizationMemberRepository extends CrudRepository<OrganizationMemberEntity, UUID> {

    OrganizationMemberEntity findById(String id);
}
