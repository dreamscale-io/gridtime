package com.dreamscale.htmflow.core.mapping


import spock.lang.Specification


class ProjectBucketMapperSpec extends Specification {

    ProjectBucketMapper bucketMapper

    def setup() {
        bucketMapper = new ProjectBucketMapper()
    }

    def "should use configuration to identify component name"() {
        given:
        bucketMapper.configureBucket("comp1", "/src/main/java/com/company/project/comp/*")

        when:
        String bucketName = bucketMapper.identifyBucket("/src/main/java/com/company/project/comp/File.java")

        then:
        assert bucketName == "comp1"
    }

    def "should match when file in root"() {
        given:
        bucketMapper.configureBucket("root", "/*")

        when:
        String bucketName = bucketMapper.identifyBucket("/File.java")

        then:
        assert bucketName == "root"
    }

    def "should use default when no bucket match"() {
        given:
        bucketMapper.configureBucket("comp1", "/src/main/java/com/company/project/other/*")

        when:
        String bucketName = bucketMapper.identifyBucket("/src/main/java/com/company/project/comp/File.java")

        then:
        assert bucketName == "default"
    }

}
