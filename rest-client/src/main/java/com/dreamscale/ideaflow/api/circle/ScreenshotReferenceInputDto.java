package com.dreamscale.ideaflow.api.circle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScreenshotReferenceInputDto {

    String fileName;
    String filePath;
}
