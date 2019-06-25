package com.dreamscale.htmflow.core.mapping

import com.dreamscale.htmflow.core.gridtime.kernel.commons.DefaultCollections
import com.dreamscale.htmflow.core.gridtime.kernel.memory.box.ProjectBoxes
import com.dreamscale.htmflow.core.gridtime.kernel.memory.box.matcher.BoxMatcherConfig
import spock.lang.Specification


class ProjectBoxesSpec extends Specification {

    ProjectBoxes projectBuckets

    def setup() {
        projectBuckets = new ProjectBoxes()
    }

    def "should match resources"() {
        given:
        projectBuckets.configureBoxMatcher(new BoxMatcherConfig("Test", "*/src/test/resources/com/nbcuniversal/forecasting*"))

        when:
        String bucketName = projectBuckets.identifyBox("/src/test/resources/com/nbcuniversal/forecasting/domain/SQLUtils.sql");

        then:
        assert bucketName == "Test"
    }

    def "should match buckets with excludes"() {
        given:
        projectBuckets.configureBoxMatcher(new BoxMatcherConfig("Test Support",
                "*/src/test/java/com/nbcuniversal/forecasting/*",
                DefaultCollections.toList(
                "*/src/test/java/com/nbcuniversal/forecasting/*IT.java",
                "*/src/test/java/com/nbcuniversal/forecasting/*Test.java")
        ));

        when:
        String bucketName = projectBuckets.identifyBox("/src/test/java/com/nbcuniversal/forecasting/controller/response/service/ForecastDetailsServiceImplTestUtils.java")

        then:
        assert bucketName == "Test Support"
    }

    def "should match controllers"() {
        given:
        projectBuckets.configureBoxMatcher(new BoxMatcherConfig("User", "*/src/main/java/com/nbcuniversal/forecasting/controller/UserController.java"));

        when:
        String bucketName = projectBuckets.identifyBox("/src/main/java/com/nbcuniversal/forecasting/controller/UserController.java")

        then:
        assert bucketName == "User"
    }

    def "should match unit test extensions"() {
        given:
        projectBuckets.configureBoxMatcher(new BoxMatcherConfig("Unit Tests", "/src/test/java/com/nbcuniversal/forecasting/*Test.java"));

        when:
        String bucketName = projectBuckets.identifyBox("/src/test/java/com/nbcuniversal/forecasting/controller/UserControllerTest.java")

        then:
        assert bucketName == "Unit Tests"
    }

    def "should use configuration to identify component name"() {
        given:
        projectBuckets.configureBoxMatcher(new BoxMatcherConfig("comp1", "/src/main/java/com/company/project/comp/*"))

        when:
        String bucketName = projectBuckets.identifyBox("/src/main/java/com/company/project/comp/File.java")

        then:
        assert bucketName == "comp1"
    }

    def "should match when file in root"() {
        given:
        projectBuckets.configureBoxMatcher(new BoxMatcherConfig("root", "/*"))

        when:
        String bucketName = projectBuckets.identifyBox("/File.java")

        then:
        assert bucketName == "root"
    }

    def "should use default when no bucket match"() {
        given:
        projectBuckets.configureBoxMatcher(new BoxMatcherConfig("comp1", "/src/main/java/com/company/project/other/*"))

        when:
        String bucketName = projectBuckets.identifyBox("/src/main/java/com/company/project/comp/File.java")

        then:
        assert bucketName == "default"
    }

}
