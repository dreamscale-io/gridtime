package com.dreamscale.htmflow.core.mapping


import spock.lang.Specification


class ProjectBucketsSpec extends Specification {

    ProjectBuckets projectBuckets

    def setup() {
        projectBuckets = new ProjectBuckets()
    }

    ///src/main/java/com/nbcuniversal/forecasting/config/MappingConfig.java
    ///src/main/java/com/nbcuniversal/forecasting/domain/BigDecimalUtils.java
    ////src/main/java/com/nbcuniversal/forecasting/config/DevDirectClient.java
    ///src/test/java/com/nbcuniversal/forecasting/controller/response/service/ForecastDetailsServiceImplTestUtils.java"

    def "should match resources"() {
        given:
        projectBuckets.configureBucket("Test", "*/src/test/resources/com/nbcuniversal/forecasting*")

        when:
        String bucketName = projectBuckets.identifyBucket("/src/test/resources/com/nbcuniversal/forecasting/domain/SQLUtils.sql");

        then:
        assert bucketName == "Test"
    }

    def "should match buckets with excludes"() {
        given:
        projectBuckets.configureBucketWithExclusions("Test Support",
                "*/src/test/java/com/nbcuniversal/forecasting/*",
                "*/src/test/java/com/nbcuniversal/forecasting/*IT.java",
                "*/src/test/java/com/nbcuniversal/forecasting/*Test.java"
        );

        when:
        String bucketName = projectBuckets.identifyBucket("/src/test/java/com/nbcuniversal/forecasting/controller/response/service/ForecastDetailsServiceImplTestUtils.java")

        then:
        assert bucketName == "Test Support"
    }

    def "should match controllers"() {
        given:
        projectBuckets.configureBucket("User", "*/src/main/java/com/nbcuniversal/forecasting/controller/UserController.java");

        when:
        String bucketName = projectBuckets.identifyBucket("/src/main/java/com/nbcuniversal/forecasting/controller/UserController.java")

        then:
        assert bucketName == "User"
    }

    def "should match unit test extensions"() {
        given:
        projectBuckets.configureBucket("Unit Tests", "/src/test/java/com/nbcuniversal/forecasting/*Test.java");

        when:
        String bucketName = projectBuckets.identifyBucket("/src/test/java/com/nbcuniversal/forecasting/controller/UserControllerTest.java")

        then:
        assert bucketName == "Unit Tests"
    }

    def "should use configuration to identify component name"() {
        given:
        projectBuckets.configureBucket("comp1", "/src/main/java/com/company/project/comp/*")

        when:
        String bucketName = projectBuckets.identifyBucket("/src/main/java/com/company/project/comp/File.java")

        then:
        assert bucketName == "comp1"
    }

    def "should match when file in root"() {
        given:
        projectBuckets.configureBucket("root", "/*")

        when:
        String bucketName = projectBuckets.identifyBucket("/File.java")

        then:
        assert bucketName == "root"
    }

    def "should use default when no bucket match"() {
        given:
        projectBuckets.configureBucket("comp1", "/src/main/java/com/company/project/other/*")

        when:
        String bucketName = projectBuckets.identifyBucket("/src/main/java/com/company/project/comp/File.java")

        then:
        assert bucketName == "default"
    }

}
