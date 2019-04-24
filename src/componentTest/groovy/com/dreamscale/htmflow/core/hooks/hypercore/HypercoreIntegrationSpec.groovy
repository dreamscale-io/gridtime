package com.dreamscale.htmflow.core.hooks.hypercore

import com.dreamscale.htmflow.ComponentTest
import org.junit.Ignore
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

@ComponentTest
public class HypercoreIntegrationSpec extends Specification {


    @Autowired
    HypercoreConnectionFactory hypercoreConnectionFactory


//    def "should connect and create a feed"() {
//
//        given:
//        HypercoreConnection connection = hypercoreConnectionFactory.connect();
//
//        when:
//        def hypercoreDto = connection.create()
//
//        then:
//        println hypercoreDto
//        assert hypercoreDto != null
//        assert hypercoreDto.getResponse().getDiscoveryKey() != null
//    }

    //HypercoreDto(action=create, status=success, timestamp=1556139600614, message=new,
    // response=HypercoreKeysDto(
    // keys={
    // discoveryKey=21a111c85d5e29764f206dc711cc3c861617bb77b42ccd340dd3ea37a32f05d1,
    // key=e3ebd9a05434be467dcef4343aa0a7b0eac3204534f34fc18169aec2929c9d57,
    // secretKey=c6de9758c7b9cf195603231f931a9a7e283af1e625adfbda6600766229666ac3e3ebd9a05434be467dcef4343aa0a7b0eac3204534f34fc18169aec2929c9d57}))

//    def "should create a feed then post to it"() {
//
//        given:
//
//        HypercoreConnection connectionWithoutAuth = hypercoreConnectionFactory.connect();
//        HypercoreKeysDto keysDto = connectionWithoutAuth.create().getResponse();
//
//        HypercoreConnection connection = hypercoreConnectionFactory.connect(keysDto.getKey(), keysDto.getSecretKey());
//        Map<String, String> properties = new HashMap<>();
//        properties.put("test", "test");
//
//        when:
//        AppendResponseDto response = connection.append(keysDto.getDiscoveryKey(), properties);
//
//        then:
//        println response
//        assert response != null
//    }

    def "should post to a feed created earlier"() {

        given:

        HypercoreConnection connection = hypercoreConnectionFactory.connect(
                "6bf6b8ea3221c0bd034fcb208d179a58bd583852a74f78ac63e437d4fae86ad1",
                "74041c617cc5b85ece10487e826787cb9bf0deb445b2b66450a434ff08fa5a036bf6b8ea3221c0bd034fcb208d179a58bd583852a74f78ac63e437d4fae86ad1");

        Map<String, String> properties = new HashMap<>();
        properties.put("test", "test");

        when:
        AppendResponseDto response = connection.append("be2504fd4fada1326c5eaaa7c23464dadf800338dc349c90962586ef02ab0189", properties);

        then:
        println response
        assert response != null
    }

}
