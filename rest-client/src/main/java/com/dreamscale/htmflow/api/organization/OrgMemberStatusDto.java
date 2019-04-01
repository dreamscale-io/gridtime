package com.dreamscale.htmflow.api.organization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrgMemberStatusDto {
    private UUID memberId;
    private String email;
    private String fullName;

    private LocalDateTime lastActivity;
    private String memberStatus;
}
