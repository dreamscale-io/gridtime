package com.dreamscale.htmflow.core.domain.uri;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity(name = "uri_within_flow")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UriWithinFlowEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    private FlowUriObjectType objectType;

    private String objectKey;

    private String uri;

    private String relativePath;

}
