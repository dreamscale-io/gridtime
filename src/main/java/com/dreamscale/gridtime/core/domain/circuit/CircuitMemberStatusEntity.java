package com.dreamscale.gridtime.core.domain.circuit;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "circuit_member_status_view")
@Data
@Builder
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
public class CircuitMemberStatusEntity {


    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID circuitId;

    private String circuitName;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID ownerId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID organizationId;


    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID memberId;

    private String shortName;
    private String fullName;

    private LocalDateTime wtfJoinTime;

    @Enumerated(EnumType.STRING)
    private RoomMemberStatus wtfRoomStatus;

    private LocalDateTime retroJoinTime;

    @Enumerated(EnumType.STRING)
    private RoomMemberStatus retroRoomStatus;



}
//    create view circuit_member_status_view as
//    select coalesce(w.circuit_id, r.circuit_id) circuit_id,
//    coalesce(w.circuit_name, r.circuit_name) circuit_name,
//    coalesce(w.owner_id, r.owner_id) owner_id,
//    coalesce(w.organization_id, r.organization_id) organization_id,
//    coalesce(w.member_id, r.member_id) member_id,
//    w.join_time wtf_join_time,
//    w.last_active wtf_last_active,
//    w.active_status wtf_active_status,
//    r.join_time retro_join_time,
//    r.last_active retro_last_active,
//    r.active_status retro_active_status,
//    coalesce(w.short_name, r.short_name) short_name,
//    coalesce(w.full_name, r.full_name) full_name
//    from wtf_member_status_view w full outer join retro_member_status_view r
//    on w.circuit_id = r.circuit_id
//        and w.member_id = r.member_id;
