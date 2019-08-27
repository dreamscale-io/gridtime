package com.dreamscale.gridtime.core;

import com.dreamscale.gridtime.api.activity.*;
import com.dreamscale.gridtime.api.batch.*;
import com.dreamscale.gridtime.api.event.RandomSnippetEventBuilder;
import com.dreamscale.gridtime.api.journal.RandomIntentionInputDtoBuilder;
import com.dreamscale.gridtime.core.domain.circle.CircleMessageRepository;
import com.dreamscale.gridtime.core.domain.circle.RandomCircleMessageEntityBuilder;
import com.dreamscale.gridtime.core.domain.flow.FlowActivityRepository;
import com.dreamscale.gridtime.core.domain.flow.RandomFlowActivityEntityBuilder;
import com.dreamscale.gridtime.core.domain.journal.*;
import com.dreamscale.gridtime.core.domain.member.*;
import com.dreamscale.gridtime.core.domain.work.RandomWorkItemToAggregateEntityBuilder;
import com.dreamscale.gridtime.core.domain.work.WorkItemToAggregateRepository;
import com.dreamscale.gridtime.core.hooks.jira.dto.RandomJiraProjectDtoBuilder;
import com.dreamscale.gridtime.core.hooks.jira.dto.RandomJiraTaskDtoBuilder;
import com.dreamscale.gridtime.core.hooks.jira.dto.RandomJiraUserDtoBuilder;
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
    private WorkItemToAggregateRepository workItemToAggregateRepository;

    @Autowired
    private IntentionRepository intentionRepository;

    @Autowired
    private FlowActivityRepository flowActivityRepository;

    @Autowired
    private CircleMessageRepository circleMessageRepository;

    @Autowired
    private MasterAccountRepository masterAccountRepository;

    @Autowired
    private SpiritXPRepository spiritXPRepository;

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

    public RandomSpiritXPEntityBuilder spiritXPEntity() {
        return new RandomSpiritXPEntityBuilder(spiritXPRepository);
    }

    public RandomTaskEntityBuilder taskEntity() {
        return new RandomTaskEntityBuilder(taskRepository);
    }

    public RandomIntentionEntityBuilder intentionEntity() { return new RandomIntentionEntityBuilder(intentionRepository);}

    public RandomFlowActivityEntityBuilder flowActivityEntity() { return new RandomFlowActivityEntityBuilder(flowActivityRepository);}

    public RandomCircleMessageEntityBuilder circleMessageEntity() { return new RandomCircleMessageEntityBuilder(circleMessageRepository);}

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

    public RandomWorkItemToAggregateEntityBuilder workItem() {
        return new RandomWorkItemToAggregateEntityBuilder(workItemToAggregateRepository);
    }

    public RandomIdleActivityBuilder idleActivity() { return new RandomIdleActivityBuilder(); }

    public RandomExternalActivityBuilder externalActivity() { return new RandomExternalActivityBuilder(); }

    public RandomBatchEventBuilder event() { return new RandomBatchEventBuilder(); }

    public RandomSnippetEventBuilder snippetEvent() { return new RandomSnippetEventBuilder(); }


}
