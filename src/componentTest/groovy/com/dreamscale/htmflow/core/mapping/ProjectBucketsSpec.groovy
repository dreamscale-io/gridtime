package com.dreamscale.htmflow.core.mapping


import spock.lang.Specification


class ProjectBucketsSpec extends Specification {

    ProjectBuckets projectBuckets

    def setup() {
        projectBuckets = new ProjectBuckets()
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
