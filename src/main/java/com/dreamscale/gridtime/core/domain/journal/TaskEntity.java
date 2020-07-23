package com.dreamscale.gridtime.core.domain.journal;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity(name = "task")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskEntity implements External {

    public static final String DEFAULT_TASK_NAME = "No Task";
    public static final String DEFAULT_TASK_DESCRIPTION = "(No Task Selected)";

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;
    private String name;
    private String lowercaseName;

    private String description;
    private String status;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "project_id")
    private UUID projectId;

    public void configureDefault() {
        this.name = DEFAULT_TASK_NAME;
        this.lowercaseName = DEFAULT_TASK_NAME.toLowerCase();
        this.description = DEFAULT_TASK_DESCRIPTION;
    }

    public boolean isDefault() {
        return name != null && name.equals(DEFAULT_TASK_NAME);
    }
}
