package com.dreamscale.htmflow.api.organization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExternalMemberDto {
    private String externalId;
    private String orgEmail;
    private String fullName;
}