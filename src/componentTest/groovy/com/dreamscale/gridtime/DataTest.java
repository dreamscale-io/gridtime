package com.dreamscale.gridtime;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ActiveProfiles({"local", "dataTest"})
@SpringBootTest(
        classes = {ComponentTestConfig.class, GridTime.class},
        properties = {"server.port=10000", "management.port=10001"},
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
public @interface DataTest {

}