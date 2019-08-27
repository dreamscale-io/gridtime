package com.dreamscale.gridtime.api.activity;

import java.time.LocalDateTime;

public interface Activity {

    Long getDurationInSeconds();

    LocalDateTime getEndTime();
}
