package com.dreamscale.gridtime.api.terminal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommandManualDto {

    private List<CommandManualPageDto> manualPages;


    public void addPage(CommandManualPageDto manPage)
    {
        if (manualPages == null) {
           manualPages = new ArrayList<>();
        }

        manualPages.add(manPage);
    }
}
