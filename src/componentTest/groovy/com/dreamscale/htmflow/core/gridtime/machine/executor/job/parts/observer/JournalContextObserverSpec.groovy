package com.dreamscale.htmflow.core.gridtime.machine.executor.job.parts.observer


import com.dreamscale.htmflow.core.domain.journal.JournalEntryEntity
import com.dreamscale.htmflow.core.domain.journal.ProjectEntity
import com.dreamscale.htmflow.core.domain.journal.TaskEntity
import com.dreamscale.htmflow.core.domain.flow.FinishStatus
import com.dreamscale.htmflow.core.gridtime.capabilities.cmd.returns.MusicGridResults
import com.dreamscale.htmflow.core.gridtime.machine.executor.job.parts.fetch.flowable.FlowableJournalEntry
import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock
import com.dreamscale.htmflow.core.gridtime.machine.executor.job.parts.source.Window
import com.dreamscale.htmflow.core.gridtime.machine.memory.cache.FeatureCache
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.track.TrackSetName
import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.GridTile
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

public class JournalContextObserverSpec extends Specification {

    JournalContextObserver journalContextObserver
    GridTile gridTile
    GeometryClock clock
    FeatureCache featureCache
    UUID torchieId

    def setup() {
        clock = new GeometryClock(LocalDateTime.now())
        journalContextObserver = new JournalContextObserver()
        torchieId = UUID.randomUUID()
        featureCache = new FeatureCache()
        gridTile = new GridTile(torchieId, clock.getActiveGridTime(), featureCache)
    }

    def "should create project & task switch events"() {
        given:
        ProjectEntity project = aRandom.projectEntity().name("p1").build();
        TaskEntity task1 = aRandom.taskEntity().name("t1").forProject(project).build();
        TaskEntity task2 = aRandom.taskEntity().name("t2").forProject(project).build();

        LocalDateTime time1 = clock.getActiveGridTime().getClockTime()
        LocalDateTime time2 = time1.plusMinutes(2);
        LocalDateTime time3 = time2.plusMinutes(3);
        LocalDateTime time4 = time3.plusMinutes(5);

        FlowableJournalEntry journalEntry1 = createJournalEvent(project, task1, time1, time2)
        FlowableJournalEntry journalEntry2 = createJournalEvent(project, task2, time2, time3)
        FlowableJournalEntry journalEntry3 = createJournalEvent(project, task2, time3, time4)

        def flowables = [journalEntry1, journalEntry2, journalEntry3] as List
        Window window = new Window(time1, time1.plusMinutes(20))
        window.addAll(flowables);

        when:
        journalContextObserver.see(window, gridTile)

        gridTile.finishAfterLoad()

        MusicGridResults tileOutput = gridTile.getMusicGrid().playTrackSet(TrackSetName.WorkContext)
        print tileOutput

        then:
        assert tileOutput != null

        //TODO combine the contexts, to always do Intentions, and handle the details of context shifting internally
        //trying to do it here, just introduces unnecessary complexity
    }


    def "should only end open intentions within window"() {
        given:

        LocalDateTime time1 = aRandom.localDateTime()
        LocalDateTime time2 = time1.plusMinutes(20);
        LocalDateTime time3 = time2.plusMinutes(20);
        LocalDateTime time4 = time3.plusMinutes(20);

        clock = new GeometryClock(time1);
        gridTile = new GridTile(torchieId, clock.getActiveGridTime(), featureCache);

        ProjectEntity project = aRandom.projectEntity().build();
        TaskEntity task1 = aRandom.taskEntity().forProject(project).build();

        FlowableJournalEntry journalEntry1 = createJournalEvent(project, task1, time1, time3)
        journalEntry1.get().setFinishStatus(FinishStatus.aborted.name())

        def flowables = [journalEntry1] as List
        Window window = new Window(time1, time2)
        window.addAll(flowables);

        journalContextObserver.see(window, gridTile)

        GridTile nextTile = new GridTile(torchieId, clock.getActiveGridTime().panRight().panRight(), featureCache);
        nextTile.initFromCarryOverContext(gridTile.getCarryOverContext());
        Window nextWindow = new Window(time3, time4)

        when:
        journalContextObserver.see(nextWindow, nextTile)
        MusicGridResults tileOutput = gridTile.getMusicGrid().playTrackSet(TrackSetName.WorkContext)
        print tileOutput

        then:
        assert tileOutput != null

    }

    FlowableJournalEntry createJournalEvent(ProjectEntity project, TaskEntity task, LocalDateTime time,
                                            LocalDateTime finishTime) {
        JournalEntryEntity journalEntryEntity = new JournalEntryEntity()

        journalEntryEntity.id = UUID.randomUUID()
        journalEntryEntity.projectId = project.id
        journalEntryEntity.taskId = task.id
        journalEntryEntity.position = time
        journalEntryEntity.finishTime = finishTime
        journalEntryEntity.finishStatus = "done"
        journalEntryEntity.description = aRandom.text(20)
        journalEntryEntity.projectName = project.name
        journalEntryEntity.taskName = task.name
        journalEntryEntity.taskSummary = aRandom.text(30)

        return new FlowableJournalEntry(journalEntryEntity)
    }
}
