package com.dreamscale.gridtime.core.domain.circuit;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "team_learning_circuit")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamLearningCircuitEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID organizationOwnerId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID createdByOwnerId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID moderatedByMemberId;

    private UUID teamId;
    private String circuitName;

}

//    create table team_learning_circuit (
//        id uuid primary key not null,
//        organization_owner_id uuid,
//        created_by_owner_id  uuid,
//        moderated_by_member_id uuid,
//        team_id  uuid,
//        circuit_name text
//);