package com.dreamscale.gridtime.core.domain.circuit;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TalkRoomRepository extends CrudRepository<TalkRoomEntity, UUID> {

    TalkRoomEntity findById(UUID roomId);

    TalkRoomEntity findByOrganizationIdAndRoomName(UUID organizationId, String roomName);

    @Query(nativeQuery = true, value = "select * from talk_room tr  " +
            "where organization_id = (:organizationId) " +
            "and exists ( select 1 from talk_room_member trm " +
            "where tr.id = trm.room_id and trm.member_id = (:memberId) "+
            "order by tr.room_name ) ")
    List<TalkRoomEntity> findRoomsByMembership(@Param("organizationId") UUID organizationId, @Param("memberId") UUID memberId);

    TalkRoomEntity findByOrganizationIdAndId(UUID organizationId, UUID roomId);
}
