package com.dreamscale.htmflow.core.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface MemberStatusRepository extends CrudRepository<MemberStatusEntity, UUID> {

    List<MemberStatusEntity> findByOrganizationId(UUID organizationId);

}
