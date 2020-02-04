package com.dreamscale.gridtime.core.domain.circuit;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "team_circuit_talk_room_view")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamCircuitTalkRoomEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID organizationId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID teamId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID ownerId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID moderatorId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID talkRoomId;

    private String teamName;

    private String circuitRoomName;

    private String talkRoomName;

    private LocalDateTime openTime;

    private LocalDateTime closeTime;

    @Enumerated(EnumType.STRING)
    private CircuitStatus circuitStatus;

    private String description;

    private String jsonTags;

}
