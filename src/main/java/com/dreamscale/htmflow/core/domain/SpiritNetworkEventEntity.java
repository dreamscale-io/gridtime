package com.dreamscale.htmflow.core.domain;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "spirit_network_event")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SpiritNetworkEventEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;
    private LocalDateTime position;
    private String eventType;
    private String description;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID networkId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID sourceSpirit;

    //metadata field with a list of guids for all the spiritIds in the network
    private String connectedSpirits;

    //miscellaneous property bag
    private String metadata;

}
