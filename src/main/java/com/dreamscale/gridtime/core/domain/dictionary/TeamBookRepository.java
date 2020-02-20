package com.dreamscale.gridtime.core.domain.dictionary;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TeamBookRepository extends CrudRepository<TeamBookEntity, UUID> {

    TeamBookEntity findByTeamIdAndLowerCaseBookName(UUID teamId, String bookName);

    @Query(nativeQuery = true, value = "select * from team_book " +
            "where book_status = 'ACTIVE' "+
            "and team_id = (:teamId) "+
            "order by lower_case_book_name ")
    List<TeamBookEntity> findByTeamId(@Param("teamId") UUID teamId);
}
