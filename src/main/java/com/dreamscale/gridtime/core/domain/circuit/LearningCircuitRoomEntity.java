package com.dreamscale.gridtime.core.domain.circuit;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.util.UUID;

@Entity(name = "learning_circuit_room_view")
@Data
@EqualsAndHashCode(of = "roomId")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LearningCircuitRoomEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID roomId;

    private String roomName;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID organizationId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID circuitId;

    private String circuitName;

    private UUID circuitOwnerId;

    private UUID circuitModeratorId;

    @Enumerated(EnumType.STRING)
    private LearningCircuitState circuitState;

}
