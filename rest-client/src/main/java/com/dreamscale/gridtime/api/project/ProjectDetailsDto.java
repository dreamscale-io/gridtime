package com.dreamscale.gridtime.api.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDetailsDto {

    private UUID id;
    private String name;
    private String description;
    private String externalId;

    private boolean isPrivate;

    private ProjectBoxConfigurationInputDto boxConfiguration;

    private List<AccessGrantDto> accessGrants;

    public void addAccessGrant(AccessGrantDto grant) {
        if (accessGrants == null) {
            accessGrants = new ArrayList<>();
        }
        accessGrants.add(grant);
    }
}
