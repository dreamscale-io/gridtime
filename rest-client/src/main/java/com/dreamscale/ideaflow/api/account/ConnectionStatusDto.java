package com.dreamscale.ideaflow.api.account;

import com.dreamscale.ideaflow.api.status.Status;
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
    private Status status;
    private String message;
    private UUID connectionId;
}
