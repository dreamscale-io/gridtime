package com.dreamscale.gridtime.core.domain.dictionary;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TeamBookWordOverrideRepository extends CrudRepository<TeamBookWordOverrideEntity, UUID> {


    @Query(nativeQuery = true, value = "select wo.* from team_book_word bw, team_book_word_override wo " +
            "where bw.team_book_id = (:bookId) "+
            "and bw.id = wo.team_book_word_id "+
            "and bw.modified_status = 'MODIFIED' ")
    List<TeamBookWordOverrideEntity> findWordOverridesByBookId(@Param("bookId") UUID bookId);


    @Query(nativeQuery = true, value = "select wo.* from team_book_word bw, team_book_word_override wo " +
            "where bw.team_book_id = (:teamBookId) "+
            "and bw.id = wo.team_book_word_id "+
            "and bw.team_word_id = (:teamWordId)")
    TeamBookWordOverrideEntity findWordOverrideByBookIdAndWordId(@Param("teamBookId") UUID teamBookId,
                                                         @Param("teamWordId") UUID teamWordId);

    @Query(nativeQuery = true, value = "select wo.* from team_book_word bw, team_book_word_override wo " +
            "where bw.team_book_id = (:teamBookId) "+
            "and bw.id = wo.team_book_word_id "+
            "and wo.lower_case_word_name = (:lowerCaseWordName)")
    TeamBookWordOverrideEntity findWordOverrideByBookIdAndLowerCaseWordName(@Param("teamBookId") UUID teamBookId, @Param("lowerCaseWordName") String lowerCaseWordName);
}
