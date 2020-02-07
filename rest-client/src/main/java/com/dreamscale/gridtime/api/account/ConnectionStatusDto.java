package com.dreamscale.gridtime.api.account;

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
public class ConnectionStatusDto {

    private UUID connectionId;
    private Status status;
    private String message;

    private UUID organizationId;
    private UUID teamId;
    private UUID memberId;

    private String userName;

}
