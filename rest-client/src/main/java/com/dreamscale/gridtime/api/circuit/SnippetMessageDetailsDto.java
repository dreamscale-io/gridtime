package com.dreamscale.gridtime.api.circuit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SnippetMessageDetailsDto implements MessageDetailsBody {

    private String sourceType;
    private String filePath;
    private Integer lineNumber;

    private String snippet;

}
