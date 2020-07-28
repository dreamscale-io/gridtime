package com.dreamscale.gridtime.core.domain.circuit;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface RoomMemberStatusRepository extends CrudRepository<RoomMemberStatusEntity, UUID> {


    List<RoomMemberStatusEntity> findByRoomId(UUID roomId);


    RoomMemberStatusEntity findByRoomIdAndMemberId(UUID roomId, UUID memberId);
}
