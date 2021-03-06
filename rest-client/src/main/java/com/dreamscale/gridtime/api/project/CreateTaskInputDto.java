package com.dreamscale.gridtime.api.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateTaskInputDto {

    private String name;
    private String description;

    private boolean isPrivate;
}
