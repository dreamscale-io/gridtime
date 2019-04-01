package com.dreamscale.htmflow.core.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface TeamMemberWorkStatusRepository extends CrudRepository<TeamMemberWorkStatusEntity, UUID> {

    List<TeamMemberWorkStatusEntity> findByOrganizationIdAndTeamId(UUID organizationId, UUID teamId);

}
