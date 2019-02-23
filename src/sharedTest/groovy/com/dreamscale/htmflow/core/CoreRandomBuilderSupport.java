package com.dreamscale.htmflow.core;

import com.dreamscale.htmflow.api.activity.*;
import com.dreamscale.htmflow.api.batch.*;
import com.dreamscale.htmflow.api.event.RandomSnippetEventBuilder;
import com.dreamscale.htmflow.api.journal.RandomIntentionInputDtoBuilder;
import com.dreamscale.htmflow.core.domain.*;
import com.dreamscale.htmflow.core.hooks.jira.dto.RandomJiraProjectDtoBuilder;
import com.dreamscale.htmflow.core.hooks.jira.dto.RandomJiraTaskDtoBuilder;
import com.dreamscale.htmflow.core.hooks.jira.dto.RandomJiraUserDtoBuilder;
import org.springframework.beans.factory.annotation.Autowired;

public class CoreRandomBuilderSupport {

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private OrganizationMemberRepository organizationMemberRepository;
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private MasterAccountRepository masterAccountRepository;

    public RandomProjectEntityBuilder projectEntity() {
        return new RandomProjectEntityBuilder(projectRepository);
    }

    public RandomOrganizationEntityBuilder organizationEntity() {
        return new RandomOrganizationEntityBuilder(organizationRepository);
    }

    public RandomMasterAccountEntityBuilder masterAccountEntity() {
        return new RandomMasterAccountEntityBuilder(masterAccountRepository);
    }

    public RandomOrganizationMemberEntityBuilder memberEntity() {
        return new RandomOrganizationMemberEntityBuilder(organizationMemberRepository);
    }


    public RandomTaskEntityBuilder taskEntity() {
        return new RandomTaskEntityBuilder(taskRepository);
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
