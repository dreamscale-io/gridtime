package com.dreamscale.gridtime.core.domain.circuit.message;

import com.dreamscale.gridtime.core.hooks.talk.dto.CircuitMessageType;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "talk_room_message")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TalkRoomMessageEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID fromId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID toRoomId;

    //TODO add these new props in the DB, useful query props to improve observability
//    private String uri;
//
//    private String request;
//
//    private String urn;

    private LocalDateTime position;

    private Long nanoTime;

    @Enumerated(EnumType.STRING)
    private CircuitMessageType messageType;

    private String jsonBody;
}


//    create table talk_room_message (
//        id uuid primary key not null,
//        from_id uuid,
//        to_room_id uuid,
//        message_time timestamp,
//        message_type text,
//        json_body text
//);
//
//    create table talk_direct_message (
//        id uuid primary key not null,
//        from_id uuid,
//        to_id uuid,
//        message_time timestamp,
//        message_type text,
//        json_body text
//);

