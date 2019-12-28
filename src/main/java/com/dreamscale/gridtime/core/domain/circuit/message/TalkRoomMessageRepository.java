package com.dreamscale.gridtime.core.domain.circuit.message;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface TalkRoomMessageRepository extends CrudRepository<TalkRoomMessageEntity, UUID> {

    List<TalkRoomMessageEntity> findByToRoomId(UUID toRoomId);

    List<TalkRoomMessageEntity> findByFromId(UUID fromId);
}
