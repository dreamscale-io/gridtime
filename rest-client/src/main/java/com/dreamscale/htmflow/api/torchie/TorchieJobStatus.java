package com.dreamscale.htmflow.api.torchie;

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
public class TorchieJobStatus {

    private UUID torchieId;
    private LocalDateTime jobStartTime;
    private LocalDateTime lastStatusUpdate;
    private String lastMetronomeTick;
    private int tilesProcessed;

    public TorchieJobStatus(UUID torchieId, String lastMetronomeTick) {
        this.torchieId = torchieId;
        this.jobStartTime = LocalDateTime.now();
        this.lastStatusUpdate = jobStartTime;

        this.lastMetronomeTick = lastMetronomeTick;
    }
}
