package com.dreamscale.htmflow.core.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface IntentionRepository extends CrudRepository<IntentionEntity, UUID> {

    @Query(nativeQuery = true, value = "select * from intention " +
            "where member_id=(:memberId) " +
            "order by position desc limit (:limit)")
    List<IntentionEntity> findByMemberIdWithLimit(@Param("memberId") UUID memberId, @Param("limit") int limit);

   // List<IntentionEntity> findIntentionsByMemberIdWithinRange(UUID memberId, LocalDateTime start, LocalDateTime end);
}

//findTop3ByLastname

//    id uuid primary key not null,
//        position timestamp,
//        description text,
//        project_id uuid,
//        task_id uuid,
//        organization_id uuid,
//        member_id uuid