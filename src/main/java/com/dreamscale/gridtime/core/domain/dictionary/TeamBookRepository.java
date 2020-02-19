package com.dreamscale.gridtime.core.domain.dictionary;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface TeamBookRepository extends CrudRepository<TeamBookEntity, UUID> {

    TeamBookEntity findByTeamIdAndLowerCaseBookName(UUID teamId, String bookName);

    List<TeamBookEntity> findByTeamId(UUID teamId);
}
