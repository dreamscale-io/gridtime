package com.dreamscale.htmflow.api.organization;

import com.dreamscale.htmflow.api.status.Status;
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
    private String name;

    private String jiraSiteUrl;
    private Status connectionStatus;
    private String connectionFailedMessage;

    private String inviteLink;
    private String inviteToken;
}
