package com.dreamscale.gridtime.core.domain.circuit.message;

import com.dreamscale.gridtime.core.hooks.talk.dto.CircuitMessageType;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "talk_direct_message")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TalkDirectMessageEntity {
    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID fromId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID toId;

    private LocalDateTime position;

    @Enumerated(EnumType.STRING)
    private CircuitMessageType messageType;

    private String jsonBody;
}



