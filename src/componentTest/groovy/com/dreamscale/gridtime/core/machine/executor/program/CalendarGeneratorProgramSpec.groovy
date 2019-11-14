package com.dreamscale.gridtime.core.machine.executor.program;

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.core.domain.time.GridTimeCalendarEntity
import com.dreamscale.gridtime.core.domain.time.GridTimeCalendarRepository
import com.dreamscale.gridtime.core.machine.GridTimeEngine
import com.dreamscale.gridtime.core.machine.capabilities.cmd.TorchieCmd
import com.dreamscale.gridtime.core.machine.Torchie;
import com.dreamscale.gridtime.core.machine.TorchieFactory
import com.dreamscale.gridtime.core.machine.GridTimeExecutor
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel
import com.dreamscale.gridtime.core.machine.executor.worker.DefaultWorkerPool
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

        DefaultWorkerPool workerPool = new DefaultWorkerPool()
        when:

        workerPool.addTorchie(torchie)

        while (workerPool.hasWork()) {
            workerPool.whatsNext().call()
        }
        

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
