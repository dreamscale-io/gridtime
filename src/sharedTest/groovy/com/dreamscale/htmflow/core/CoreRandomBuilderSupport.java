package com.dreamscale.htmflow.core;

import com.dreamscale.htmflow.api.journal.RandomIntentionInputDtoBuilder;
import com.dreamscale.htmflow.core.domain.RandomOrganizationEntityBuilder;
import com.dreamscale.htmflow.core.domain.RandomOrganizationMemberEntityBuilder;
import com.dreamscale.htmflow.core.domain.RandomProjectEntityBuilder;
import com.dreamscale.htmflow.core.domain.RandomTaskEntityBuilder;
import com.dreamscale.htmflow.core.hooks.jira.dto.RandomJiraProjectDtoBuilder;
import com.dreamscale.htmflow.core.hooks.jira.dto.RandomJiraTaskDtoBuilder;
import com.dreamscale.htmflow.core.hooks.jira.dto.RandomJiraUserDtoBuilder;

public class CoreRandomBuilderSupport {

    public RandomProjectEntityBuilder projectEntity() {
        return new RandomProjectEntityBuilder();
    }

    public RandomOrganizationEntityBuilder organizationEntity() {
        return new RandomOrganizationEntityBuilder();
    }

    public RandomOrganizationMemberEntityBuilder memberEntity() {
        return new RandomOrganizationMemberEntityBuilder();
    }


    public RandomTaskEntityBuilder taskEntity() {
        return new RandomTaskEntityBuilder();
    }


    public RandomIntentionInputDtoBuilder intentionInputDto() {
        return new RandomIntentionInputDtoBuilder();
    }

    public RandomJiraProjectDtoBuilder jiraProjectDto() { return new RandomJiraProjectDtoBuilder(); }

    public RandomJiraTaskDtoBuilder jiraTaskDto() { return new RandomJiraTaskDtoBuilder(); }

    public RandomJiraUserDtoBuilder jiraUserDto() { return new RandomJiraUserDtoBuilder(); }

}
