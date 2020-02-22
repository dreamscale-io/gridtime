package com.dreamscale.gridtime.core.machine.executor.worker;

import lombok.Data;

import java.util.UUID;

@Data
public class WorkerClaim {

    private UUID workerId;
}
