package com.dreamscale.gridtime.core.domain.dictionary;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TeamBookTagRepository extends CrudRepository<TeamBookTagEntity, UUID> {

    List<TeamBookTagEntity> findByTeamBookId(UUID bookId);

    @Query(nativeQuery = true, value = "select bt.* from team_book_tag bt, team_book b " +
            "where bt.team_book_id = b.id " +
            "and b.team_id = (:teamId) "+
            "and b.lower_case_book_name = (:lowerCaseBook) "+
            "and bt.team_tag_id = (:tagId) ")
    TeamBookTagEntity findByTeamBookNameAndTagId(@Param("teamId") UUID teamId, @Param("lowerCaseBook") String lowerCaseBook, @Param("tagId") UUID tagId);
}
