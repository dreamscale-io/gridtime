package org.dreamscale.htmflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(SwaggerConfig.class)
@ComponentScan("org.dreamscale.htmflow")
public class HtmFlow {

    public static void main(String[] args) {
        SpringApplication.run(HtmFlow.class, args);
    }

}
