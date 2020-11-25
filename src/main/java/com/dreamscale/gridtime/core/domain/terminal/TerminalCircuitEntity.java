package com.dreamscale.gridtime.core.domain.terminal;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "terminal_circuit")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TerminalCircuitEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID organizationId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID creatorId;

    private String circuitName;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID talkRoomId;


    private LocalDateTime createdDate;

}
