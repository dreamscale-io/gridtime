package com.dreamscale.ideaflow.api.journal;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskReferenceInputDto {

    private String taskName;
}
