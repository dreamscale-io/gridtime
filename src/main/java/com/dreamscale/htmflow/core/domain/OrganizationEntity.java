package com.dreamscale.htmflow.core.domain;

import com.dreamscale.htmflow.api.status.Status;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity(name = "organization")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;
    @Column(name = "org_name")
    private String orgName;

    @Column(name = "domain_name")
    private String domainName;

    @Column(name = "jira_site_url")
    private String jiraSiteUrl;

    @Column(name = "jira_user")
    private String jiraUser;

    @Column(name = "jira_api_key")
    private String jiraApiKey;

}
