package com.dreamscale.ideaflow.core.feeds.story.see;

import com.dreamscale.ideaflow.core.domain.JournalEntryEntity;
import com.dreamscale.ideaflow.core.domain.flow.FinishStatus;
import com.dreamscale.ideaflow.core.feeds.common.Flowable;
import com.dreamscale.ideaflow.core.feeds.story.StoryFrame;
import com.dreamscale.ideaflow.core.feeds.executor.parts.fetch.flowable.FlowableJournalEntry;
import com.dreamscale.ideaflow.core.feeds.story.feature.context.IdeaFlowContextBeginningEvent;
import com.dreamscale.ideaflow.core.feeds.story.feature.context.IdeaFlowContextEndingEvent;
import com.dreamscale.ideaflow.core.feeds.story.feature.context.IdeaFlowStructureLevel;

import java.util.List;

/**
 * Identifies the beginning of tasks and intentions as the beginning and ending of songs,
 * at two levels of story abstraction
 */
public class IdeaFlowJournalContextObserver implements IdeaFlowObserver {

    @Override
    public void see(StoryFrame currentStoryFrame, Window window) {

        List<Flowable> flowables = window.getFlowables();

        IdeaFlowContextBeginningEvent lastOpenProject = currentStoryFrame.getCurrentContext(IdeaFlowStructureLevel.PROJECT);
        IdeaFlowContextBeginningEvent lastOpenTask = currentStoryFrame.getCurrentContext(IdeaFlowStructureLevel.TASK);
        IdeaFlowContextBeginningEvent lastOpenIntention = currentStoryFrame.getCurrentContext(IdeaFlowStructureLevel.INTENTION);

        addQueuedEndingIfInWindow(currentStoryFrame, window);

        for (Flowable flowable : flowables) {
            if (flowable instanceof FlowableJournalEntry) {
                JournalEntryEntity journalEntry = ((JournalEntryEntity) flowable.get());

                createIntentionDoneIfNotNull(currentStoryFrame, journalEntry, lastOpenIntention);
                createTaskDoneIfSwitched(currentStoryFrame, journalEntry, lastOpenTask);
                createProjectDoneIfSwitched(currentStoryFrame, journalEntry, lastOpenProject);

                createProjectStartIfSwitched(currentStoryFrame, journalEntry, lastOpenProject);
                createTaskStartIfSwitched(currentStoryFrame, journalEntry, lastOpenTask);
                createIntentionStartAndEnd(window, currentStoryFrame, journalEntry);

                lastOpenProject = currentStoryFrame.getCurrentContext(IdeaFlowStructureLevel.PROJECT);
                lastOpenTask = currentStoryFrame.getCurrentContext(IdeaFlowStructureLevel.TASK);
                lastOpenIntention = currentStoryFrame.getCurrentContext(IdeaFlowStructureLevel.INTENTION);

            }
        }

    }

    private void addQueuedEndingIfInWindow(StoryFrame currentStoryFrame, Window window) {
        IdeaFlowContextEndingEvent endWhenInWindow = currentStoryFrame.getSavedContextToAddWhenInWindow();
        if (endWhenInWindow != null && window.isWithin(endWhenInWindow.getPosition())) {
            currentStoryFrame.endContext(endWhenInWindow);
            currentStoryFrame.saveContextToAddWhenInWindow(null);
        }
    }

    private void createTaskStartIfSwitched(StoryFrame currentStoryFrame, JournalEntryEntity journalEntry, IdeaFlowContextBeginningEvent lastOpenTask) {
        if (lastOpenTask == null || !lastOpenTask.getReferenceId().equals(journalEntry.getTaskId())) {
            IdeaFlowContextBeginningEvent taskBeginning = createTaskBeginning(journalEntry);
            currentStoryFrame.beginContext(taskBeginning);
        }
    }

    private void createProjectStartIfSwitched(StoryFrame currentStoryFrame, JournalEntryEntity journalEntry, IdeaFlowContextBeginningEvent lastOpenProject) {
        if (lastOpenProject == null || !lastOpenProject.getReferenceId().equals(journalEntry.getProjectId())) {
            IdeaFlowContextBeginningEvent projectBeginning = createProjectBeginning(journalEntry);
            currentStoryFrame.beginContext(projectBeginning);
        }
    }

    private void createProjectDoneIfSwitched(StoryFrame currentStoryFrame, JournalEntryEntity journalEntry, IdeaFlowContextBeginningEvent lastOpenProject) {
        if (lastOpenProject != null && !lastOpenProject.getReferenceId().equals(journalEntry.getProjectId())) {
            IdeaFlowContextEndingEvent projectEnding = createProjectEnding(journalEntry, lastOpenProject);
            currentStoryFrame.endContext(projectEnding);
        }
    }

    private void createTaskDoneIfSwitched(StoryFrame currentStoryFrame, JournalEntryEntity journalEntry, IdeaFlowContextBeginningEvent lastOpenTask) {
        if (lastOpenTask != null && !lastOpenTask.getReferenceId().equals(journalEntry.getTaskId())) {
            IdeaFlowContextEndingEvent taskEnding = createTaskEnding(journalEntry, lastOpenTask);
            currentStoryFrame.endContext(taskEnding);
        }
    }

    private IdeaFlowContextBeginningEvent createProjectBeginning(JournalEntryEntity journalEntry) {
        IdeaFlowContextBeginningEvent projectBeginning = new IdeaFlowContextBeginningEvent();
        projectBeginning.setReferenceId(journalEntry.getProjectId());
        projectBeginning.setStructureLevel(IdeaFlowStructureLevel.PROJECT);
        projectBeginning.setName(journalEntry.getProjectName());
        projectBeginning.setPosition(journalEntry.getPosition());

        return projectBeginning;
    }

    private IdeaFlowContextBeginningEvent createTaskBeginning(JournalEntryEntity journalEntry) {
        IdeaFlowContextBeginningEvent taskBeginning = new IdeaFlowContextBeginningEvent();
        taskBeginning.setReferenceId(journalEntry.getTaskId());
        taskBeginning.setStructureLevel(IdeaFlowStructureLevel.TASK);
        taskBeginning.setName(journalEntry.getTaskName());
        taskBeginning.setDescription(journalEntry.getTaskSummary());
        taskBeginning.setPosition(journalEntry.getPosition());

        return taskBeginning;
    }

    private IdeaFlowContextEndingEvent createTaskEnding(JournalEntryEntity journalEntry, IdeaFlowContextBeginningEvent lastTaskStart) {
        IdeaFlowContextEndingEvent taskEnding = new IdeaFlowContextEndingEvent();
        taskEnding.setReferenceId(lastTaskStart.getReferenceId());
        taskEnding.setStructureLevel(IdeaFlowStructureLevel.TASK);
        taskEnding.setName(lastTaskStart.getName());
        taskEnding.setDescription(lastTaskStart.getDescription());
        taskEnding.setPosition(journalEntry.getPosition());
        taskEnding.setFinishStatus(IdeaFlowContextEndingEvent.FinishStatus.SUCCESS);

        return taskEnding;
    }

    private IdeaFlowContextEndingEvent createProjectEnding(JournalEntryEntity journalEntry, IdeaFlowContextBeginningEvent lastOpenProject) {
        IdeaFlowContextEndingEvent projectEnding = new IdeaFlowContextEndingEvent();
        projectEnding.setReferenceId(lastOpenProject.getReferenceId());
        projectEnding.setStructureLevel(IdeaFlowStructureLevel.PROJECT);
        projectEnding.setName(lastOpenProject.getName());
        projectEnding.setPosition(journalEntry.getPosition());
        projectEnding.setFinishStatus(IdeaFlowContextEndingEvent.FinishStatus.SUCCESS);

        return projectEnding;
    }


    private void createIntentionStartAndEnd(Window window, StoryFrame storyFrame, JournalEntryEntity journalEntry) {

        IdeaFlowContextBeginningEvent intentionStart = new IdeaFlowContextBeginningEvent();
        intentionStart.setReferenceId(journalEntry.getId());
        intentionStart.setStructureLevel(IdeaFlowStructureLevel.INTENTION);
        intentionStart.setDescription(journalEntry.getDescription());
        intentionStart.setPosition(journalEntry.getPosition());

        storyFrame.beginContext(intentionStart);

        if (journalEntry.getFinishTime() != null) {
            IdeaFlowContextEndingEvent intentionEnd = new IdeaFlowContextEndingEvent();
            intentionEnd.setReferenceId(journalEntry.getId());
            intentionEnd.setStructureLevel(IdeaFlowStructureLevel.INTENTION);
            intentionEnd.setFinishStatus(decodeFinishStatus(journalEntry.getFinishStatus()));
            intentionEnd.setDescription(journalEntry.getDescription());
            intentionEnd.setPosition(journalEntry.getFinishTime());

            if (window.isWithin(journalEntry.getFinishTime())) {
                storyFrame.endContext(intentionEnd);
            } else {
                storyFrame.saveContextToAddWhenInWindow(intentionEnd);
            }

        }
    }

    private void createIntentionDoneIfNotNull(StoryFrame storyFrame, JournalEntryEntity journalEntry, IdeaFlowContextBeginningEvent lastIntentionStart) {
        IdeaFlowContextEndingEvent intentionEnd = null;

        if (lastIntentionStart != null) {
            intentionEnd = new IdeaFlowContextEndingEvent();
            intentionEnd.setReferenceId(lastIntentionStart.getReferenceId());
            intentionEnd.setStructureLevel(IdeaFlowStructureLevel.INTENTION);
            intentionEnd.setFinishStatus(IdeaFlowContextEndingEvent.FinishStatus.SUCCESS);
            intentionEnd.setDescription(lastIntentionStart.getDescription());
            intentionEnd.setPosition(journalEntry.getPosition());

            storyFrame.endContext(intentionEnd);
        }

    }

    private IdeaFlowContextEndingEvent.FinishStatus decodeFinishStatus(String journalFinishStatus) {
        IdeaFlowContextEndingEvent.FinishStatus songFinishStatus = null;

        if (journalFinishStatus != null) {
            FinishStatus finishStatus = FinishStatus.valueOf(journalFinishStatus);

            switch (finishStatus) {
                case done:
                    songFinishStatus = IdeaFlowContextEndingEvent.FinishStatus.SUCCESS;
                    break;
                case aborted:
                    songFinishStatus = IdeaFlowContextEndingEvent.FinishStatus.ABORT;
                    break;
            }
        }

        return songFinishStatus;
    }
}
