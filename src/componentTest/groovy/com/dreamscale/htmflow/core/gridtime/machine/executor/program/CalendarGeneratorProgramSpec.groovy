package com.dreamscale.htmflow.core.gridtime.machine.executor.program;

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.core.domain.time.GridTimeDayPartsEntity
import com.dreamscale.htmflow.core.domain.time.GridTimeDayPartsRepository
import com.dreamscale.htmflow.core.domain.time.GridTimeDaysEntity
import com.dreamscale.htmflow.core.domain.time.GridTimeDaysRepository
import com.dreamscale.htmflow.core.domain.time.GridTimeTwentiesEntity
import com.dreamscale.htmflow.core.domain.time.GridTimeTwentiesRepository
import com.dreamscale.htmflow.core.gridtime.capabilities.cmd.TorchieCmd
import com.dreamscale.htmflow.core.gridtime.machine.Torchie;
import com.dreamscale.htmflow.core.gridtime.machine.TorchieFactory
import com.dreamscale.htmflow.core.gridtime.machine.TorchiePoolExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import spock.lang.Specification;

@ComponentTest
class CalendarGeneratorProgramSpec extends Specification {

    @Autowired
    TorchieFactory torchieFactory
    TorchiePoolExecutor torchieExecutor

    @Autowired
    GridTimeTwentiesRepository gridTimeTwentiesRepository

    @Autowired
    GridTimeDayPartsRepository gridTimeDayPartsRepository

    @Autowired
    GridTimeDaysRepository gridTimeDaysRepository

    def setup() {
        torchieExecutor = new TorchiePoolExecutor(1);
    }

    def "should generate 72 twenties and aggregate rollups for dayparts and days"() {
        given:
        Torchie torchie = torchieFactory.wireUpCalendarTorchie(73)
        TorchieCmd torchieCmd = new TorchieCmd(torchieExecutor, torchie)

        when:
        torchieCmd.runProgram()

        Iterable<GridTimeTwentiesEntity> twenties = gridTimeTwentiesRepository.findAll()

        Iterable<GridTimeDayPartsEntity>  dayParts = gridTimeDayPartsRepository.findAll()

        Iterable<GridTimeDaysEntity> days = gridTimeDaysRepository.findAll()

        then:
        assert twenties != null
        assert twenties.size() == 73

        assert dayParts != null
        assert dayParts.size() == 6

        assert days != null
        assert days.size() == 1
    }
}
