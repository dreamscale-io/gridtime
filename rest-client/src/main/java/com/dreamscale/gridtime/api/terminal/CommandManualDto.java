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

    private Map<ActivityContext, CommandManualPageDto> manualPagesByActivityType;

    public void addPage(ActivityContext context, CommandManualPageDto contextPage) {
        if (activityContexts == null) {
            activityContexts = new ArrayList<>();
        }
        activityContexts.add(context);

        if (manualPagesByActivityType == null) {
            manualPagesByActivityType = new HashMap<>();
        }

        manualPagesByActivityType.put(context, contextPage);
    }


    public String toDisplayString() {
        String out = "";

        for (ActivityContext context : manualPagesByActivityType.keySet()) {

            CommandManualPageDto page = manualPagesByActivityType.get(context);
            out += page.toDisplayString();
        }

        return out;
    }

    public String toString() {
        return toDisplayString();
    }
}
