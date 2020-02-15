package com.dreamscale.gridtime.core.domain.dictionary;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface TeamDictionaryTagRepository extends CrudRepository<TeamDictionaryTagEntity, UUID> {

    TeamDictionaryTagEntity findByTeamIdAndTagName(UUID teamId, String tagName);
}
