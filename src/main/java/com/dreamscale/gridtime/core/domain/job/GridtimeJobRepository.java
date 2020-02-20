package com.dreamscale.gridtime.core.domain.job;

import com.dreamscale.gridtime.core.domain.dictionary.TeamDictionaryWordTombstoneEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface GridtimeJobRepository extends CrudRepository<GridtimeJobEntity, UUID> {

}
