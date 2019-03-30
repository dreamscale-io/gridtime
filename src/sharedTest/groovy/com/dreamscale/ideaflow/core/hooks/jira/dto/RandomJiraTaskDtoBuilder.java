package com.dreamscale.ideaflow.core.hooks.jira.dto;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.dreamscale.ideaflow.core.CoreARandom.aRandom;

public class RandomJiraTaskDtoBuilder extends JiraTaskDto.JiraTaskDtoBuilder {

    public RandomJiraTaskDtoBuilder() {
        super();
        expand(aRandom.text(5))
                .self(aRandom.text(5))
                .id(aRandom.numberText(2))
                .key(aRandom.text(3))
                .fields(createFieldsMap());
    }

    private Map<String, Object> currentFieldsMap;

    private Map<String, Object> createFieldsMap() {
        currentFieldsMap = new LinkedHashMap<>();
        currentFieldsMap.put("summary", "summary of a task");
        return currentFieldsMap;
    }

    public RandomJiraTaskDtoBuilder status(String statusName) {
        Map<String, Object> statusMap = new LinkedHashMap<>();
        statusMap.put("name", statusName);
        currentFieldsMap.put("status", statusMap);

        fields(currentFieldsMap);
        return this;
    }

}
