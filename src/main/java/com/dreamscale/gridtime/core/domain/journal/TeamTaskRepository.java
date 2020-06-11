package com.dreamscale.gridtime.core.domain.journal;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface TeamTaskRepository extends CrudRepository<TeamTaskEntity, UUID> {

    TeamTaskEntity findByTeamIdAndTeamProjectIdAndLowercaseName(UUID teamId, UUID teamProjectId, String lowercaseName);

    List<TeamTaskEntity> findByTeamIdAndLowercaseName(UUID teamId, String standardizedTaskName);
}
