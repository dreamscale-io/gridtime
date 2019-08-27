package com.dreamscale.gridtime.core.hooks.jira.dto;

import static com.dreamscale.gridtime.core.CoreARandom.aRandom;

public class RandomJiraProjectDtoBuilder extends JiraProjectDto.JiraProjectDtoBuilder {

    public RandomJiraProjectDtoBuilder() {
        super();
        expand(aRandom.text(5))
                .self(aRandom.text(5))
                .id(aRandom.numberText(2))
                .key(aRandom.text(3))
                .name(aRandom.text(10))
                .projectTypeKey(aRandom.text(3))
                .simplified(true);
    }

}
