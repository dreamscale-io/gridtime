package com.dreamscale.gridtime.core.domain.dictionary;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface CommunityDictionaryTombstoneRepository extends CrudRepository<CommunityDictionaryTombstoneEntity, UUID> {

}
