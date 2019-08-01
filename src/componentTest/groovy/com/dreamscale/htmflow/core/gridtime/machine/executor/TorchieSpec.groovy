package com.dreamscale.htmflow.core.gridtime.machine.executor

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.core.domain.flow.FlowActivityEntity
import com.dreamscale.htmflow.core.domain.journal.IntentionEntity
import com.dreamscale.htmflow.core.domain.journal.ProjectEntity
import com.dreamscale.htmflow.core.domain.journal.TaskEntity

import com.dreamscale.htmflow.core.gridtime.machine.Torchie
import com.dreamscale.htmflow.core.gridtime.machine.TorchieFactory
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.instructions.TileInstructions
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.feed.service.CalendarService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

@ComponentTest
class TorchieSpec extends Specification {

//    @Autowired
//    StoryTileRepository tileRepository;

    @Autowired
    TorchieFactory torchieFactory

    @Autowired
    CalendarService calendarService

    UUID torchieId
    UUID teamId
    LocalDateTime clockStart

    def "should create executable torchie job that doesn't splode"() {
        given:
        torchieId = UUID.randomUUID();
        teamId = UUID.randomUUID();

        clockStart = LocalDateTime.of(2019, 1, 7, 0, 0)

        Torchie torchie = torchieFactory.wireUpTeamMemberTorchie(teamId, torchieId, clockStart)

        calendarService.saveCalendar(1, 15, torchie.getActiveTick().from)
        calendarService.saveCalendar(1, 2, torchie.getActiveTick().from.zoomOut())


        LocalDateTime time1_0 = aRandom.localDateTime()
        LocalDateTime time2_0 = time1_0.plusMinutes(20);
        LocalDateTime time2_5 = time2_0.plusMinutes(10);

        createActivity(torchieId, time1_0, time2_0)
        createEvent(torchieId, time2_0)
        createEvent(torchieId, time2_5)

        List<TileInstructions> allInstructions = new ArrayList<>()

        when:
        for (int i = 0; i < 15; i++) {
            TileInstructions instructions = torchie.whatsNext();
            instructions.call()

            allInstructions.add(instructions);
        }

        then:
        assert allInstructions.size() == 15

        for (TileInstructions instruction : allInstructions) {
            assert instruction.isSuccessful()
        }

        assert allInstructions.get(0).getOutputTile() != null

    }

    void createEvent(UUID memberId, LocalDateTime time) {
        ProjectEntity projectEntity = aRandom.projectEntity().save();
        TaskEntity taskEntity = aRandom.taskEntity().forProject(projectEntity).save();

        IntentionEntity journalEntry = aRandom.intentionEntity()
                .memberId(memberId)
                .position(time)
                .projectId(projectEntity.id)
                .taskId(taskEntity.id)
                .save()

    }

    void createActivity(UUID memberId, LocalDateTime start, LocalDateTime end) {

        FlowActivityEntity flowActivityEntity = aRandom.flowActivityEntity()
                .memberId(memberId)
                .start(start)
                .end(end)
                .save()

    }
}
