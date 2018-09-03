package com.dreamscale.htmflow.core.domain;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity(name = "config_project_sync")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConfigProjectSyncEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID organizationId;

    @Column(name = "project_external_id")
    private String projectExternalId;

}
