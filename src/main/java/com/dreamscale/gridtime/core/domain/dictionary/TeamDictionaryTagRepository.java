package com.dreamscale.gridtime.core.domain.dictionary;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TeamDictionaryTagRepository extends CrudRepository<TeamDictionaryTagEntity, UUID> {

    TeamDictionaryTagEntity findByTeamIdAndLowerCaseTagName(UUID teamId, String tagName);


    @Query(nativeQuery = true, value = "select * from team_dictionary_tag  " +
            "where team_id = (:teamId) "+
            "and definition is null ")
    List<TeamDictionaryTagEntity> findByTeamIdAndBlankDefinition(@Param("teamId") UUID teamId);
}
