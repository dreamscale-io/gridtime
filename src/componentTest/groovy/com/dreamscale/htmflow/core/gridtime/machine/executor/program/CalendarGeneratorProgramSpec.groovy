package com.dreamscale.htmflow.core.gridtime.machine.executor.program;

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.core.domain.time.GridTimeCalendarEntity
import com.dreamscale.htmflow.core.domain.time.GridTimeCalendarRepository

import com.dreamscale.htmflow.core.gridtime.capabilities.cmd.TorchieCmd
import com.dreamscale.htmflow.core.gridtime.machine.Torchie;
import com.dreamscale.htmflow.core.gridtime.machine.TorchieFactory
import com.dreamscale.htmflow.core.gridtime.machine.TorchiePoolExecutor
import com.dreamscale.htmflow.core.gridtime.machine.clock.ZoomLevel;
import org.springframework.beans.factory.annotation.Autowired;
import spock.lang.Specification;

@ComponentTest
class CalendarGeneratorProgramSpec extends Specification {

    @Autowired
    TorchieFactory torchieFactory
    TorchiePoolExecutor torchieExecutor

    @Autowired
    GridTimeCalendarRepository gridTimeCalendarRepository

    def setup() {
        torchieExecutor = new TorchiePoolExecutor(1);
    }

    def "should generate 72 twenties and aggregate rollups for dayparts and days"() {
        given:
        Torchie torchie = torchieFactory.wireUpCalendarTorchie(73)
        TorchieCmd torchieCmd = new TorchieCmd(torchieExecutor, torchie)

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
