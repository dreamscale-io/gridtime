package com.dreamscale.htmflow.core.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface IntentionRepository extends CrudRepository<IntentionEntity, UUID> {

    List<IntentionEntity> findTop100ByMemberIdOrderByPosition(UUID memberId);


   // List<IntentionEntity> findChunksByMemberIdWithinRange(UUID memberId, LocalDateTime start, LocalDateTime end);
}

//findTop3ByLastname

//    id uuid primary key not null,
//        position timestamp,
//        description text,
//        project_id uuid,
//        task_id uuid,
//        organization_id uuid,
//        member_id uuid