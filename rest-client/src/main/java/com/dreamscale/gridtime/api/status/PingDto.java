package com.dreamscale.gridtime.api.status;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PingDto {

    private LocalDateTime testLocalDateTime;
    private ZonedDateTime testZonedDateTime;

}
