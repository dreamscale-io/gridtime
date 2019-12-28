package com.dreamscale.gridtime.core.domain.circuit.message;

import com.dreamscale.gridtime.core.hooks.talk.dto.TalkMessageType;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "wtf_feed_message_view")
@Data
@EqualsAndHashCode(of = "messageId")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WTFFeedMessageEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID messageId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID circuitId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID roomId;

    private String circuitName;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID fromId;

    private String fromShortName;

    private String fromFullName;

    private LocalDateTime position;

    @Enumerated(EnumType.STRING)
    private TalkMessageType messageType;

    private String jsonBody;
}

//
//    create view wtf_feed_message_view as
//    select trm.id message_id, c.id circuit_id, tr.id room_id, c.circuit_name, trm.from_id,
//        mnv.short_name from_short_name, mnv.full_name from_full_name,
//        trm.message_time, trm.message_type, json_body
//        from learning_circuit c,
//        talk_room tr,
//        talk_room_message trm,
//        member_name_view mnv;

