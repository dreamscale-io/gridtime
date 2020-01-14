package com.dreamscale.gridtime.core.domain.circuit.message;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TalkRoomMessageRepository extends CrudRepository<TalkRoomMessageEntity, UUID> {

    List<TalkRoomMessageEntity> findByToRoomId(UUID toRoomId);

    List<TalkRoomMessageEntity> findByFromId(UUID fromId);

    @Query(nativeQuery = true, value = "select trm.* from talk_room tr, talk_room_message trm " +
            "where tr.id = trm.to_room_id " +
            "and tr.room_name = (:talkRoomName) " +
            "order by position ")
    List<TalkRoomMessageEntity> findByTalkRoomName(@Param("talkRoomName") String talkRoomName);
}
