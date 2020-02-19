package com.dreamscale.gridtime.core.domain.dictionary;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TeamBookOverrideRepository extends CrudRepository<TeamBookOverrideEntity, UUID> {


    @Query(nativeQuery = true, value = "select bo.* from team_book_tag bt, team_book_override bo " +
            "where bt.team_book_id = (:bookId) "+
            "and bt.id = bo.team_book_tag_id "+
            "and bt.modified_status = 'MODIFIED' ")
    List<TeamBookOverrideEntity> findDefinitionsByBookId(@Param("bookId") UUID bookId);


}
