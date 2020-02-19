package com.dreamscale.gridtime.core.domain.dictionary;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface TeamDictionaryWordTombstoneRepository extends CrudRepository<TeamDictionaryWordTombstoneEntity, UUID> {

    List<TeamDictionaryWordTombstoneEntity> findByForwardToOrderByRipDate(UUID tagId);

    List<TeamDictionaryWordTombstoneEntity> findByTeamIdAndLowerCaseWordName(UUID teamId, String lowerCaseWordName);
}
