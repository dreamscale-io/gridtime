package com.dreamscale.gridtime.core.domain.circuit.message;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface RetroFeedMessageRepository extends CrudRepository<WTFFeedMessageEntity, UUID> {


    List<WTFFeedMessageEntity> findByCircuitIdOrderByPosition(UUID circleId);

    //TODO not sure if we need this, or what it was for?
//    @Query(nativeQuery = true, value = "select * from circle_feed_message_view cf " +
//            "where exists (select 1 from circle c where c.owner_member_id=(:memberId) and c.id=cf.circle_id) " +
//            "and position >= (:afterDate) " +
//            "order by position asc limit (:limit)")
//    List<WTFFeedMessageEntity> findByOwnerIdAfterDateWithLimit(@Param("fromMemberId") UUID memberId,
//                                                                  @Param("afterDate") Timestamp afterDate,
//                                                                  @Param("limit") int limit);


    //message status updates for circuit status events
    //create a view that queries this table
    //make circuit_message entities return jsonBody objects that we can mirror the Dtos
    //update_circuit_message feeds for status in gridtime engine

}
