package com.dreamscale.htmflow.core.mapping


import spock.lang.Specification


class ProjectBucketsSpec extends Specification {

    ProjectBuckets projectBuckets

    def setup() {
        projectBuckets = new ProjectBuckets()
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
