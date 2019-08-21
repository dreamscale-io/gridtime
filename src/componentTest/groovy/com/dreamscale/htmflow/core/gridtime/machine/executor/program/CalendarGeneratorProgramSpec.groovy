package com.dreamscale.htmflow.core.gridtime.machine.executor.program;

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.core.domain.time.GridTimeCalendarEntity
import com.dreamscale.htmflow.core.domain.time.GridTimeCalendarRepository

import com.dreamscale.htmflow.core.gridtime.capabilities.cmd.TorchieCmd
import com.dreamscale.htmflow.core.gridtime.machine.Torchie;
import com.dreamscale.htmflow.core.gridtime.machine.TorchieFactory
import com.dreamscale.htmflow.core.gridtime.machine.GridTimeExecutor
import com.dreamscale.htmflow.core.gridtime.machine.clock.ZoomLevel
import com.dreamscale.htmflow.core.gridtime.machine.executor.workpile.TorchieWorkerPool;
import org.springframework.beans.factory.annotation.Autowired;
import spock.lang.Specification;

@ComponentTest
class CalendarGeneratorProgramSpec extends Specification {

    @Autowired
    TorchieFactory torchieFactory

    @Autowired
    GridTimeCalendarRepository gridTimeCalendarRepository



    def "should generate 72 twenties and aggregate rollups for dayparts and days"() {
        given:
        Torchie torchie = torchieFactory.wireUpCalendarTorchie(73)

        GridTimeExecutor gridTimeExecutor = new GridTimeExecutor(new TorchieWorkerPool());
        TorchieCmd torchieCmd = new TorchieCmd(gridTimeExecutor, torchie)

        when:
        torchieCmd.runProgram()

        List<GridTimeCalendarEntity> twenties = gridTimeCalendarRepository.findByZoomLevel(ZoomLevel.TWENTY)

        List<GridTimeCalendarEntity> dayParts = gridTimeCalendarRepository.findByZoomLevel(ZoomLevel.DAY_PART)

        List<GridTimeCalendarEntity> days = gridTimeCalendarRepository.findByZoomLevel(ZoomLevel.DAY)


        then:
        assert twenties != null
        assert twenties.size() == 73

        assert dayParts != null
        assert dayParts.size() == 6

        assert days != null
        assert days.size() == 1
    }
}
