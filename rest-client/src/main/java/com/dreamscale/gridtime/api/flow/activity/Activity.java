package com.dreamscale.gridtime.api.flow.activity;

import java.time.LocalDateTime;

public interface Activity {

    Long getDurationInSeconds();

    LocalDateTime getEndTime();
}
