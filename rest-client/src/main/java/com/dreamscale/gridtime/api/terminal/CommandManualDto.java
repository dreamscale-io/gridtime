package com.dreamscale.gridtime.api.terminal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommandManualDto {

    private List<ActivityContext> activityContexts;

    private Map<ActivityContext, CommandManualPageDto> manualPagesByActivityContext;

    public void addPage(ActivityContext context, CommandManualPageDto contextPage) {
        if (activityContexts == null) {
            activityContexts = new ArrayList<>();
        }
        activityContexts.add(context);

        if (manualPagesByActivityContext == null) {
            manualPagesByActivityContext = new HashMap<>();
        }

        manualPagesByActivityContext.put(context, contextPage);
    }

    public CommandManualPageDto getManualPage(ActivityContext context) {
        return manualPagesByActivityContext.get(context);
    }


    public String toDisplayString() {
        String out = "";

        for (ActivityContext context : manualPagesByActivityContext.keySet()) {

            CommandManualPageDto page = manualPagesByActivityContext.get(context);
            out += page.toDisplayString();
        }

        return out;
    }

    public String toString() {
        return toDisplayString();
    }
}
