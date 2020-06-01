package com.dreamscale.gridtime.api.terminal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RunCommandInputDto {

    private Command command;

    private String argumentStr;
}
