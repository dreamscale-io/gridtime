package com.dreamscale.gridtime.api.circuit;

import com.dreamscale.gridtime.api.flow.event.SnippetSourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DescriptionInputDto {

    private String description;

}
