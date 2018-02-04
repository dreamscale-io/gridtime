package com.dreamscale.htmflow.api.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HeartbeatDto {

    private int idleTime;
    private int deltaTime;
}
