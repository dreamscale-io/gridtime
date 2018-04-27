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
public class MembershipDto {
    private UUID memberId;
    private String orgEmail;

    private Status bindingStatus;

    private ExternalMemberDto boundExternalAccount;

    private MasterAccountDto masterAccountDto;

    private OrganizationDto organization;

}
