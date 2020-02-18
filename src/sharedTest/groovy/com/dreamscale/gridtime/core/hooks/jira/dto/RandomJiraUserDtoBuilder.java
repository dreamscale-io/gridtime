package com.dreamscale.gridtime.core.hooks.jira.dto;

import static com.dreamscale.gridtime.core.CoreARandom.aRandom;

public class RandomJiraUserDtoBuilder extends JiraUserDto.JiraUserDtoBuilder {

    public RandomJiraUserDtoBuilder() {
        super();
        self(aRandom.text(5))
                .accountId(aRandom.numberText(2))
                .accountType("atlassian")
                .active(true)
                .emailAddress(aRandom.email())
                .displayName(aRandom.name());
    }


}
