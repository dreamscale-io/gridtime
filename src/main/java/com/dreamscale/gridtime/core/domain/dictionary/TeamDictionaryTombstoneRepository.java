package com.dreamscale.gridtime.core.domain.dictionary;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface TeamDictionaryTombstoneRepository extends CrudRepository<TeamDictionaryTombstoneEntity, UUID> {

}
