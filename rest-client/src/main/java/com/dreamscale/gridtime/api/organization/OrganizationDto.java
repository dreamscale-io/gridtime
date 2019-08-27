package com.dreamscale.gridtime.api.organization;

import com.dreamscale.gridtime.api.status.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationDto {
    private UUID id;
    private String orgName;
    private String domainName;

    private String jiraSiteUrl;
    private Status connectionStatus;
    private String connectionFailedMessage;

    private String inviteLink;
    private String inviteToken;
}
