package com.dreamscale.htmflow.api.circle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CircleDto {

    UUID id;
    String circleName;
    String problemDescription;

    LocalDateTime startTime;
    LocalDateTime endTime;
    LocalDateTime lastResumeTime;

    List<CircleMemberDto> members;

    Long durationInSeconds;
    Boolean onShelf;

    UUID channelId;

}
