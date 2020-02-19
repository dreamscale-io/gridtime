package com.dreamscale.gridtime.core.domain.dictionary;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface TeamBookWordTombstoneRepository extends CrudRepository<TeamBookWordTombstoneEntity, UUID> {

    List<TeamBookWordTombstoneEntity> findByTeamIdAndLowerCaseWordName(UUID teamId, String lowerCaseWordName);
}
