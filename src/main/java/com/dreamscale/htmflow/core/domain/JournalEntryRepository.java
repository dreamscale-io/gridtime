package com.dreamscale.htmflow.core.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public interface JournalEntryRepository extends CrudRepository<JournalEntryEntity, UUID> {

    @Query(nativeQuery = true, value = "select * from journal_entry_view " +
            "where member_id=(:memberId) " +
            "order by position desc limit (:limit)")
    List<JournalEntryEntity> findByMemberIdWithLimit(@Param("memberId") UUID memberId, @Param("limit") int limit);


    @Query(nativeQuery = true, value = "select * from journal_entry_view " +
            "where member_id=(:memberId) " +
            "and position < (:beforeDate) " +
            "order by position desc limit (:limit)")
    List<JournalEntryEntity> findByMemberIdBeforeDateWithLimit(@Param("memberId") UUID memberId,
                                                            @Param("beforeDate") Timestamp beforeDate,
                                                            @Param("limit") int limit);

}
