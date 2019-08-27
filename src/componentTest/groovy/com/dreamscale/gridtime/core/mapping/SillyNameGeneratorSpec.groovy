package com.dreamscale.gridtime.core.mapping

import spock.lang.Specification

class SillyNameGeneratorSpec extends Specification {

    SillyNameGenerator sillyNameGenerator

    def setup() {
        sillyNameGenerator = new SillyNameGenerator()
    }

    def "should generate random silly name"() {
        when:
        String randomName = sillyNameGenerator.random()

        then:
        println randomName
        assert randomName != null
    }
}
