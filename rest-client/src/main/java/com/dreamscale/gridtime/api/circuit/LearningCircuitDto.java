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

    private Long totalCircuitElapsedNanoTime;
    private Long totalCircuitPausedNanoTime;

    private Long wtfOpenNanoTime;
    private Long retroOpenNanoTime;
    private Long closeCircuitNanoTime;
    private Long solvedCircuitNanoTime;
    private Long pauseCircuitNanoTime;
    private Long resumeCircuitNanoTime;
    private Long cancelCircuitNanoTime;

    private LocalDateTime openTime;
    private String openTimeStr;

    private String circuitState;

    private Integer marksForReview;
    private Integer marksRequiredForReview;

    private Integer marksForClose;
    private Integer marksRequiredForClose;

    public void setOpenTime(LocalDateTime openTime) {
        this.openTime = openTime;
        this.openTimeStr = TimeFormatter.format(openTime);
    }
}
