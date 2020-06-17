package com.dreamscale.gridtime.core.domain.journal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "project")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectEntity implements External {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;
    private String name;
    private String lowercaseName;

    private String description;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "external_id")
    private String externalId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID createdBy;

    private LocalDateTime createdDate;

    private boolean isPrivate;


}
//    create table project_grant_access (
//        id uuid primary key not null,
//        project_id uuid not null,
//        access_type text not null,
//        access_id uuid not null,
//        granted_by_id uuid not null,
//        granted_date timestamp
//);
