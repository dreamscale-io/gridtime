package com.dreamscale.gridtime.core.domain.circuit.message;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public interface WTFFeedMessageRepository extends CrudRepository<WTFFeedMessageEntity, UUID> {


    List<WTFFeedMessageEntity> findByCircuitIdOrderByPosition(UUID circuitId);

    @Query(nativeQuery = true, value = "select * from wtf_feed_message_view cf " +
            "where exists (select 1 from circuit c where c.owner_id=(:memberId) and c.id=cf.circuit_id) " +
            "and position >= (:afterDate) " +
            "order by position asc limit (:limit)")
    List<WTFFeedMessageEntity> findByOwnerIdAfterDateWithLimit(UUID memberId, Timestamp valueOf, int fetchSize);

//    create view wtf_feed_message_view as
//    select trm.id message_id, c.id circuit_id, tr.id room_id, c.circuit_name, trm.from_id,
//    mnv.short_name from_short_name, mnv.full_name from_full_name,
//    trm.message_time, trm.message_type, json_body
//    from learning_circuit c,
//    talk_room tr,
//    talk_room_message trm,
//    member_name_view mnv
//    where c.wtf_room_id = tr.id and tr.id = trm.to_room_id and trm.from_id = mnv.member_id;
//


    //message status updates for circuit status events
    //create a view that queries this table
    //make circuit_message entities return jsonBody objects that we can mirror the Dtos
    //update_circuit_message feeds for status in gridtime engine

}
