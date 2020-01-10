package com.dreamscale.gridtime.core.domain.circuit;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TalkRoomMemberRepository extends CrudRepository<TalkRoomMemberEntity, UUID> {

    TalkRoomMemberEntity findByOrganizationIdAndRoomIdAndMemberId(UUID organizationId, UUID roomId, UUID memberId);

    List<TalkRoomMemberEntity> findByRoomId(UUID roomId);

    List<TalkRoomMemberEntity> findByMemberId(UUID memberId);

    @Query(nativeQuery = true, value = "select * from talk_room tr, talk_room_member trm " +
            "where tr.id = trm.room_id " +
            "and tr.organization_id = (:organizationId) " +
            "and trm.member_id = (:memberId) " +
            "and  tr.talk_room_id = (:talkRoomId) ")
    TalkRoomMemberEntity findByOrganizationMemberAndTalkRoomId(@Param("organizationId") UUID organizationId,
                                               @Param("memberId") UUID memberId,
                                               @Param("talkRoomId") String talkRoomId);
}
