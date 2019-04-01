package com.dreamscale.htmflow;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Sql(scripts = "classpath:/db/test_cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@ActiveProfiles({"local", "componentTest"})
@SpringBootTest(
        classes = {ComponentTestConfig.class, HtmFlow.class},
        properties = {"server.port=10000", "management.port=10001"},
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
public @interface ComponentTest {

}