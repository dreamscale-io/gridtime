package com.dreamscale.gridtime.core.domain.circuit;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

public interface TalkRoomMemberRepository extends CrudRepository<TalkRoomMemberEntity, UUID> {

    TalkRoomMemberEntity findByOrganizationIdAndRoomIdAndMemberId(UUID organizationId, UUID roomId, UUID memberId);

    List<TalkRoomMemberEntity> findByRoomId(UUID roomId);

    List<TalkRoomMemberEntity> findByMemberId(UUID memberId);

    @Query(nativeQuery = true, value = "select trm.* from talk_room tr, talk_room_member trm " +
            "where tr.id = trm.room_id " +
            "and tr.organization_id = (:organizationId) " +
            "and trm.member_id = (:memberId) " +
            "and  tr.room_name = (:talkRoomName) ")
    TalkRoomMemberEntity findByOrganizationMemberAndTalkRoomId(@Param("organizationId") UUID organizationId,
                                               @Param("memberId") UUID memberId,
                                               @Param("talkRoomName") String talkRoomId);

    @Modifying
    @Query(nativeQuery = true, value = "delete from talk_room_member trm " +
            "where trm.room_id = (:roomId) ")
    void deleteMembersInRoom(@Param("roomId") UUID roomId);


    @Modifying
    @Query(nativeQuery = true, value = "delete from talk_room_member trm " +
            "where trm.member_id = (:memberId) ")
    void deleteFromAllRooms(@Param("memberId") UUID memberId);
}
