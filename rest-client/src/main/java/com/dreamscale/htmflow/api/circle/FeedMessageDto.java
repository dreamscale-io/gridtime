package com.dreamscale.htmflow.api.circle;

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
public class FeedMessageDto {

    private UUID circleId;
    private LocalDateTime position;
    private CircleMemberDto circleMemberDto;

    private CircleMessageType messageType;

    //optional fields
    private String message;
    private String fileName;
    private String filePath;
    private String snippetSource;
    private String snippet;


}
