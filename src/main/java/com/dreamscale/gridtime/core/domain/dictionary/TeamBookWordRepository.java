package com.dreamscale.gridtime.core.domain.dictionary;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TeamBookWordRepository extends CrudRepository<TeamBookWordEntity, UUID> {

    List<TeamBookWordEntity> findByTeamBookId(UUID bookId);

    @Query(nativeQuery = true, value = "select bw.* from team_book_word bw, team_book b " +
            "where bw.team_book_id = b.id " +
            "and b.team_id = (:teamId) "+
            "and b.lower_case_book_name = (:lowerCaseBook) "+
            "and bw.team_word_id = (:wordId) ")
    TeamBookWordEntity findByTeamBookNameAndWordId(@Param("teamId") UUID teamId, @Param("lowerCaseBook") String lowerCaseBook, @Param("wordId") UUID wordId);
}
