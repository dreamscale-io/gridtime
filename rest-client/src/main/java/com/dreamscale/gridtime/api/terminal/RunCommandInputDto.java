package com.dreamscale.gridtime.api.terminal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RunCommandInputDto {

    private Command command;

    private List<String> args;

    public RunCommandInputDto(Command command, String ... inputArgs) {
        this.command = command;

        args = Arrays.asList(inputArgs);
    }
}
