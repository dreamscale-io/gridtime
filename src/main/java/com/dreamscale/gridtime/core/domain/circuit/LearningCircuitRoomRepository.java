package com.dreamscale.gridtime.core.domain.circuit;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface LearningCircuitRoomRepository extends CrudRepository<LearningCircuitRoomEntity, UUID> {

    @Query(nativeQuery = true, value = "select r.* from learning_circuit_room_view r " +
            "where exists (select 1 from talk_room_member trm " +
            "where trm.organization_id = (:organizationId) " +
            "and trm.room_id = r.room_id "+
            "and trm.member_id = (:memberId)) ")
    List<LearningCircuitRoomEntity> findRoomsByMembership(@Param("organizationId") UUID organizationId,
                                                          @Param("memberId") UUID memberId);


//



}
