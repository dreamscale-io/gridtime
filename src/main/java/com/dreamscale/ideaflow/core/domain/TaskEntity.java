package com.dreamscale.ideaflow.core.domain;

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

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;
    private String name;
    private String summary;
    private String status;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "project_id")
    private UUID projectId;

    public static final String DEFAULT_TASK_NAME = "No Task";


    public TaskEntity configureDefaultTask() {
        name = DEFAULT_TASK_NAME;
        summary = "(No Task Selected)";
        status = "In Progress";
        externalId = "-1";

        return this;
    }

    public boolean isDefaultTask() {
        return name != null && name.equals(DEFAULT_TASK_NAME);
    }
}
