package com.dreamscale.htmflow.core.gridtime.machine.memory.cache

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.core.domain.member.json.Member
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.details.AuthorsDetails
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.details.ExecutionEvent
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.details.StructureLevel
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.reference.AuthorsReference
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.reference.ExecutionReference
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.reference.FeelsReference
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.reference.WorkContextReference
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.Duration
import java.time.LocalDateTime

@ComponentTest
class FeatureResolverServiceSpec extends Specification {

    @Autowired
    FeatureResolverService featureResolver

    FeatureCache featureCache

    FeatureReferenceFactory featureReferenceFactory

    UUID teamId

    def setup() {
        teamId = UUID.randomUUID();
        featureCache = new FeatureCache();
        featureReferenceFactory = new FeatureReferenceFactory()
    }

    def "should resolve matching features and converge on same feature id"() {
        given:
        FeelsReference feelsReferenceA = featureReferenceFactory.createFeelsStateReference(-3);
        FeelsReference feelsReferenceB = featureReferenceFactory.createFeelsStateReference(-3);

        when:
        featureResolver.resolve(teamId, feelsReferenceA)
        featureResolver.resolve(teamId, feelsReferenceB)

        then:
        assert feelsReferenceA.isResolved() == true
        assert feelsReferenceA.getFeatureId() == feelsReferenceB.getFeatureId()
    }

    def "should serialize and deserialize all object types"() {
        given:
        ExecutionReference executionReference = featureCache.lookupExecutionReference(
                new ExecutionEvent(3L, LocalDateTime.now(), Duration.ofSeconds(3)))
        ExecutionReference executionReference2 = featureCache.lookupExecutionReference(
                new ExecutionEvent(3L, LocalDateTime.now(), Duration.ofSeconds(3)))


        AuthorsReference authorsReference = featureCache.lookupAuthorsReference(new AuthorsDetails([new Member("123", "Arty")]))
        AuthorsReference authorsReference2 = featureCache.lookupAuthorsReference(new AuthorsDetails([new Member("123", "Arty")]))

        WorkContextReference contextReference = featureCache.lookupContextReference(StructureLevel.TASK, UUID.randomUUID(), "taskA")
        WorkContextReference contextReference2 = featureCache.lookupContextReference(StructureLevel.TASK, contextReference.getReferenceId(), "taskB")


        when:
        featureResolver.resolve(teamId, executionReference)
        featureResolver.resolve(teamId, executionReference2)

        featureResolver.resolve(teamId, authorsReference)
        featureResolver.resolve(teamId, authorsReference2)

        featureResolver.resolve(teamId, contextReference)
        featureResolver.resolve(teamId, contextReference2)

        then:
        assert executionReference.getFeatureId() == executionReference2.getFeatureId()
        assert authorsReference.getFeatureId() == authorsReference2.getFeatureId()
        assert contextReference.getFeatureId() == contextReference2.getFeatureId()

        assert contextReference2.getDescription() == "taskA"
    }
}
