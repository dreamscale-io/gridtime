package com.dreamscale.gridtime.api.spirit;

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
public class TorchieTombstoneDto {

    private UUID id;
    private UUID spiritId;
    private String epitaph;

    private LocalDateTime dateOfBirth;
    private LocalDateTime dateOfDeath;

    private Integer level;
    private Integer totalXp;
    private String title;

}
