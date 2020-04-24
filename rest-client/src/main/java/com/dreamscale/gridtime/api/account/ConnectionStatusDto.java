package com.dreamscale.gridtime.api.account;

import com.dreamscale.gridtime.api.organization.OrganizationDto;
import com.dreamscale.gridtime.api.status.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConnectionStatusDto {

    private UUID connectionId;
    private Status status;
    private String message;

    private UUID organizationId;
    private UUID teamId;
    private UUID memberId;

    private String userName;

    private List<OrganizationDto> participatingOrganizations;

    //private List<UUID> teamIds; //teams are for the above organization (team.index(0) is home)

    //private List<UUID> myOrganizations; //participatings (should include active)

}
