package com.dreamscale.gridtime.core.domain.dictionary;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface TeamDictionaryTombstoneRepository extends CrudRepository<TeamDictionaryTombstoneEntity, UUID> {

    List<TeamDictionaryTombstoneEntity> findByForwardToOrderByRipDate(UUID tagId);

    List<TeamDictionaryTombstoneEntity> findByTeamIdAndLowerCaseTagName(UUID teamId, String lowerCaseTag);
}
