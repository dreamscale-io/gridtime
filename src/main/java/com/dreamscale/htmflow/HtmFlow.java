package com.dreamscale.htmflow;

import org.dreamscale.htmflow.SwaggerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@ComponentScan("com.dreamscale.htmflow")
@Import(SwaggerConfig.class)
public class HtmFlow {

    public static void main(String[] args) {
        SpringApplication.run(HtmFlow.class, args);
    }

}
