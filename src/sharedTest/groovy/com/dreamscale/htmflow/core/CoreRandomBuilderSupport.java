package com.dreamscale.htmflow.core;

import com.dreamscale.htmflow.api.activity.*;
import com.dreamscale.htmflow.api.batch.*;
import com.dreamscale.htmflow.api.event.RandomSnippetEventBuilder;
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

    public RandomFlowBatchBuilder flowBatch() { return new RandomFlowBatchBuilder(); }

    public RandomEditorActivityBuilder editorActivity() { return new RandomEditorActivityBuilder(); }

    public RandomExecutionActivityBuilder executionActivity() { return new RandomExecutionActivityBuilder(); }

    public RandomModificationActivityBuilder modificationActivity() { return new RandomModificationActivityBuilder(); }

    public RandomIdleActivityBuilder idleActivity() { return new RandomIdleActivityBuilder(); }

    public RandomExternalActivityBuilder externalActivity() { return new RandomExternalActivityBuilder(); }

    public RandomBatchEventBuilder event() { return new RandomBatchEventBuilder(); }

    public RandomSnippetEventBuilder snippetEvent() { return new RandomSnippetEventBuilder(); }


}
