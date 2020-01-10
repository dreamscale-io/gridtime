package com.dreamscale.gridtime.core.domain.circuit;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "talk_room_member")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TalkRoomMemberEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID roomId;

    private LocalDateTime joinTime;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID organizationId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID memberId;

    private LocalDateTime lastActive;

    @Enumerated(EnumType.STRING)
    private RoomMemberStatus roomStatus;



}

//    create table talk_room_member (
//        id uuid primary key not null,
//        room_id uuid,
//        join_time timestamp,
//        organization_id uuid,
//        team_id uuid,
//        member_id uuid,
//        last_active timestamp,
//        active_status text,
//        unique (room_id, member_id)
//);