package com.dreamscale.ideaflow.core.hooks.jira.dto;

import static com.dreamscale.ideaflow.core.CoreARandom.aRandom;

public class RandomJiraUserDtoBuilder extends JiraUserDto.JiraUserDtoBuilder {

    public RandomJiraUserDtoBuilder() {
        super();
        self(aRandom.text(5))
                .accountId(aRandom.numberText(2))
                .key(aRandom.text(3))
                .name(aRandom.text(10))
                .emailAddress(aRandom.email())
                .displayName(aRandom.name());
    }


}
