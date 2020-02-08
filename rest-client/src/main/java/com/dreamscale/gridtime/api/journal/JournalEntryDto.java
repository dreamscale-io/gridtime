package com.dreamscale.gridtime.api.journal;

import com.dreamscale.gridtime.api.shared.TimeFormatter;
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
public class JournalEntryDto {

    private UUID id;
    private LocalDateTime position;
    private String positionStr;

    private String description;
    private UUID projectId;
    private UUID taskId;

    private String taskName;
    private String taskSummary;
    private String projectName;

    private Integer flameRating;
    private String finishStatus;
    private Boolean linked;

    private JournalEntryType journalEntryType;

    public void setPosition(LocalDateTime position) {
        this.position = position;
        this.positionStr = TimeFormatter.format(position);
    }
}
