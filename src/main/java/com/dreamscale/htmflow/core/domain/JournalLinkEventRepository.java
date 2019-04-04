package com.dreamscale.htmflow.core.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public interface JournalLinkEventRepository extends CrudRepository<JournalLinkEventEntity, UUID> {

    List<JournalLinkEventEntity> findByMemberId(UUID id);

    @Query(nativeQuery = true, value = "select * from journal_link_event " +
            "where member_id=(:memberId) " +
            "and position >= (:afterDate) " +
            "order by position asc limit (:limit)")
    List<JournalLinkEventEntity> findByMemberIdAfterDateWithLimit(@Param("memberId") UUID memberId,
                                                                  @Param("afterDate") Timestamp afterDate,
                                                                  @Param("limit") int limit);

}
