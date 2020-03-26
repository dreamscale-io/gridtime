package com.dreamscale.gridtime.api.circuit;


import com.dreamscale.gridtime.api.organization.OnlineStatus;
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
public class CircuitMemberStatusDto {

    UUID memberId;
    String fullName;
    String displayName;

    String username;

    LocalDateTime lastActive;
    OnlineStatus onlineStatus;
}
