package com.dreamscale.htmflow.core.gridtime.machine.memory.cache

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.core.gridtime.machine.executor.job.parts.search.FeatureSearchService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

@ComponentTest
class FeatureSearchServiceSpec extends Specification {

    @Autowired
    FeatureSearchService featureSearchService

    def setup() {

    }

    def "should resolve matching features to same grid feature id"() {
        //test should setup a feature, and then subsequent calls should resolve to same id once saved

    }

    def "should serialize and deserialize all object types"() {
        //all the different object types should be serializable features

        //why is the feature resolver in the pool?  And not in the transform... seems odd

    }
}
