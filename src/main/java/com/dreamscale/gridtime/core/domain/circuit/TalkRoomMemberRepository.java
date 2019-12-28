package com.dreamscale.gridtime.core.domain.circuit;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface TalkRoomMemberRepository extends CrudRepository<TalkRoomMemberEntity, UUID> {

    TalkRoomMemberEntity findByOrganizationIdAndRoomIdAndMemberId(UUID organizationId, UUID roomId, UUID memberId);

    List<TalkRoomMemberEntity> findByRoomId(UUID roomId);

    List<TalkRoomMemberEntity> findByMemberId(UUID memberId);
}
