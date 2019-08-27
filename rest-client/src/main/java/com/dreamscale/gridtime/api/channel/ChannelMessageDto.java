package com.dreamscale.gridtime.api.channel;

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
public class ChannelMessageDto {

    private UUID channelId;
    private UUID fromMemberId;
    private LocalDateTime messageTime;
    private String message;

}
