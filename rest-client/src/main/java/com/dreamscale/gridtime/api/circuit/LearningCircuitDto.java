package com.dreamscale.gridtime.api.circuit;

import com.dreamscale.gridtime.api.shared.TimeFormatter;
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
public class LearningCircuitDto {

    UUID id;
    String circuitName;
    String description;
    List<String> tags;

    String wtfTalkRoomName;
    UUID wtfTalkRoomId;

    String retroTalkRoomName;
    UUID retroTalkRoomId;

    String statusTalkRoomName;
    UUID statusTalkRoomId;

    private UUID ownerId;
    private String ownerName;

    private UUID moderatorId;
    private String moderatorName;

    LocalDateTime openTime;
    String openTimeStr;

    String circuitState;

    LocalDateTime startTimerFromTime;
    String startTimerFromTimeStr;

    Long startTimerSecondsOffset;

    public void setOpenTime(LocalDateTime openTime) {
        this.openTime = openTime;
        this.openTimeStr = TimeFormatter.format(openTime);
    }

    public void setStartTimerFromTime(LocalDateTime startTimerFromTime) {
        this.startTimerFromTime = startTimerFromTime;
        this.startTimerFromTimeStr = TimeFormatter.format(startTimerFromTime);
    }
}
