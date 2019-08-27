package com.dreamscale.gridtime.core.hooks.realtime.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberInputDto {

    UUID organizationId;
    UUID teamId;
    UUID memberId;
}


