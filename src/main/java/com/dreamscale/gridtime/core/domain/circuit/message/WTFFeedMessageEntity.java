package com.dreamscale.gridtime.core.domain.circuit.message;

import com.dreamscale.gridtime.core.hooks.talk.dto.CircuitMessageType;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "wtf_feed_message_view")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WTFFeedMessageEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID circuitId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID organizationId;

    private String circuitName;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID fromMemberId;

    private String fromDisplayName;

    private String fromFullName;

    private String fromUsername;

    private LocalDateTime position;

    @Enumerated(EnumType.STRING)
    private CircuitMessageType circuitMessageType;

}
