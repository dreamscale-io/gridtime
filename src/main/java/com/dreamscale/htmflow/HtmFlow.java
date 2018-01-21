package com.dreamscale.htmflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.dreamscale.htmflow")
public class HtmFlow {

    public static void main(String[] args) {
        SpringApplication.run(HtmFlow.class, args);
    }

}
