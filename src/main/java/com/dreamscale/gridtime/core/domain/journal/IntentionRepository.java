package com.dreamscale.gridtime.core.domain.journal;

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


    @Query(nativeQuery = true, value = "select * from intention i " +
            "where i.member_id=(:memberId) " +
            "and not exists (select 1 from wtf_journal_link jl " +
            "where i.member_id=jl.member_id " +
            "and jl.intention_id = i.id )" +
            "order by position desc limit (:limit)")
    List<IntentionEntity> findRecentByMemberIdWithoutWTFsWithLimit(@Param("memberId") UUID memberId, @Param("limit") int limit);



    @Query(nativeQuery = true, value = "select * from intention " +
            "where member_id=(:memberId) " +
            "and position < (:beforeDate) " +
            "order by position desc limit (:limit)")
    List<IntentionEntity> findByMemberIdBeforeDateWithLimit(@Param("memberId") UUID memberId,
                                                            @Param("beforeDate") Timestamp beforeDate,
                                                            @Param("limit") int limit);

    @Query(nativeQuery = true, value = "select * from intention " +
            "where member_id=(:memberId) " +
            "order by position limit 1")
    IntentionEntity findFirstByMemberId(@Param("memberId") UUID memberId);

    @Query(nativeQuery = true, value = "select * from intention " +
            "order by position limit 1")
    IntentionEntity findFirst();
}
