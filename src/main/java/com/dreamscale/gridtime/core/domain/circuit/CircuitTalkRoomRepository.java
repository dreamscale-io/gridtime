package com.dreamscale.gridtime.core.domain.circuit;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CircuitTalkRoomRepository extends CrudRepository<CircuitTalkRoomEntity, UUID> {

    @Query(nativeQuery = true, value = "select * from circuit_talk_room_view r, talk_room_member trm " +
            "where r.organization_id = (:organizationId) " +
            "and r.room_id = trm.room_id "+
            "and trm.member_id = (:memberId) "+
            "and r.circuit_status = 'ACTIVE' ")
    List<CircuitTalkRoomEntity> findRoomsByMembership(@Param("organizationId") UUID organizationId,
                                                      @Param("memberId") UUID memberId);


//



}
