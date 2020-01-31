package com.dreamscale.gridtime.core.domain.circuit;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity(name = "team_circuit_room")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamCircuitRoomEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID organizationId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID teamId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID talkRoomId;

    private String localName;

    private String description;

    private String jsonTags;

}



//    create table team_circuit_room (
//        id uuid primary key not null,
//        organization_id uuid,
//        team_id uuid unique,
//        talk_room_id uuid,
//        local_name text,
//        description text,
//        jsonTags text
//};
//
