package com.dreamscale.gridtime.core.hooks.talk.dto;

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
public class TalkMessageDto {

    UUID messageId;

    String originId;
    String destinationId;

    LocalDateTime messageTime;
    String messageType;
    String jsonBody;
}
