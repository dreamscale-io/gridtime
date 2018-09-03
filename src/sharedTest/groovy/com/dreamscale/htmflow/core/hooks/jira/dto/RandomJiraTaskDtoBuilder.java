package com.dreamscale.htmflow.core.hooks.jira.dto;

import java.util.HashMap;
import java.util.Map;

import static com.dreamscale.htmflow.core.CoreARandom.aRandom;

public class RandomJiraTaskDtoBuilder extends JiraTaskDto.JiraTaskDtoBuilder {

    public RandomJiraTaskDtoBuilder() {
        super();
        expand(aRandom.text(5))
                .self(aRandom.text(5))
                .id(aRandom.numberText(2))
                .key(aRandom.text(3))
                .fields(createFieldsMap());
    }

    private Map<String, Object> createFieldsMap() {
        Map<String, Object> fieldsMap = new HashMap<>();
        fieldsMap.put("summary", "summary of a task");
        return fieldsMap;
    }

}
