package com.dreamscale.gridtime.core.domain.dictionary;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TeamDictionaryWordRepository extends CrudRepository<TeamDictionaryWordEntity, UUID> {

    TeamDictionaryWordEntity findByTeamIdAndLowerCaseWordName(UUID teamId, String tagName);


    @Query(nativeQuery = true, value = "select * from team_dictionary_word  " +
            "where team_id = (:teamId) "+
            "and definition is null ")
    List<TeamDictionaryWordEntity> findByTeamIdAndBlankDefinition(@Param("teamId") UUID teamId);



    @Query(nativeQuery = true, value = "select w.* from team_dictionary_word w, team_book_word bw " +
            "where bw.team_word_id = w.id " +
            "and bw.team_book_id = (:bookId) "+
            "and bw.modified_status = 'UNCHANGED' ")
    List<TeamDictionaryWordEntity> findWordsByBookId(@Param("bookId") UUID bookId);



}
