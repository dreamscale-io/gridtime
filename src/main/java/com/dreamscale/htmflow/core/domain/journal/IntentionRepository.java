package com.dreamscale.htmflow.core.domain.journal;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public interface IntentionRepository extends CrudRepository<IntentionEntity, UUID> {

    @Query(nativeQuery = true, value = "select * from intention " +
            "where member_id=(:memberId) " +
            "order by position desc limit (:limit)")
    List<IntentionEntity> findByMemberIdWithLimit(@Param("memberId") UUID memberId, @Param("limit") int limit);


    @Query(nativeQuery = true, value = "select * from intention " +
            "where member_id=(:memberId) " +
            "and position < (:beforeDate) " +
            "order by position desc limit (:limit)")
    List<IntentionEntity> findByMemberIdBeforeDateWithLimit(@Param("memberId") UUID memberId,
                                                            @Param("beforeDate") Timestamp beforeDate,
                                                            @Param("limit") int limit);

}
