package com.dreamscale.ideaflow.core.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface CircleFeedMessageRepository extends CrudRepository<CircleFeedMessageEntity, UUID> {

    List<CircleFeedMessageEntity> findByCircleIdOrderByTimePosition(UUID circleId);
}
