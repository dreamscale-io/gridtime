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

    private UUID ownerId;
    private String ownerName;

    private UUID moderatorId;
    private String moderatorName;

    LocalDateTime retroStartedTime;
    String retroStartedTimeStr;

    LocalDateTime openTime;
    String openTimeStr;

    LocalDateTime closeTime;
    String closeTimeStr;

    String circuitStatus;

    LocalDateTime lastOnHoldTime;
    String lastOnHoldTimeStr;

    LocalDateTime lastResumeTime;
    String lastResumeTimeStr;

    Long secondsBeforeOnHold;

    public void setRetroStartedTime(LocalDateTime retroStartedTime) {
        this.retroStartedTime = retroStartedTime;
        this.retroStartedTimeStr = TimeFormatter.format(retroStartedTime);
    }

    public void setOpenTime(LocalDateTime openTime) {
        this.openTime = openTime;
        this.openTimeStr = TimeFormatter.format(openTime);
    }

    public void setCloseTime(LocalDateTime closeTime) {
        this.closeTime = closeTime;
        this.closeTimeStr = TimeFormatter.format(closeTime);
    }

    public void setLastOnHoldTime(LocalDateTime lastOnHoldTime) {
        this.lastOnHoldTime = lastOnHoldTime;
        this.lastOnHoldTimeStr = TimeFormatter.format(lastOnHoldTime);
    }

    public void setLastResumeTime(LocalDateTime lastResumeTime) {
        this.lastResumeTime = lastResumeTime;
        this.lastResumeTimeStr = TimeFormatter.format(lastResumeTime);
    }
}
