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
public class MemberRegistrationDetailsDto {
    private UUID memberId;
    private String orgEmail;
    private String fullName;

    private UUID masterAccountId;
    private String activationCode;

}
