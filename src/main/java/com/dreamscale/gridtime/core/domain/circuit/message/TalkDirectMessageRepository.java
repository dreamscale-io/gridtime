package com.dreamscale.gridtime.core.domain.circuit.message;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface TalkDirectMessageRepository extends CrudRepository<TalkDirectMessageEntity, UUID> {

    List<TalkDirectMessageEntity> findByToId(UUID toId);

    List<TalkDirectMessageEntity> findByFromId(UUID fromId);
}
