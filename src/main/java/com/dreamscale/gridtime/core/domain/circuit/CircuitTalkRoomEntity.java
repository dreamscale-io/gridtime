package com.dreamscale.gridtime.core.domain.circuit;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "circuit_talk_room_view")
@Data
@EqualsAndHashCode(of = "roomId")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CircuitTalkRoomEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID organizationId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID roomId;

    private String roomName;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID circuitId;

    private String circuitName;

    private UUID circuitOwnerId;

    @Enumerated(EnumType.STRING)
    private CircuitStatus circuitStatus;

}
